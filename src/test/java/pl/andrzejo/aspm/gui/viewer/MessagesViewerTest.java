package pl.andrzejo.aspm.gui.viewer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.app.ApplicationClosingEvent;
import pl.andrzejo.aspm.eventbus.events.gui.FontChangedEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.factory.BeanFactory;
import pl.andrzejo.aspm.gui.OutputLogger;
import pl.andrzejo.aspm.gui.viewer.model.Message;
import pl.andrzejo.aspm.gui.viewer.model.MessageListModel;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.*;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

class MessagesViewerTest {
    private final List<Message> logged = new ArrayList<>();
    private final AtomicBoolean offEdtModelChange = new AtomicBoolean();
    private final MessageListModel model = new MessageListModel();
    private OutputLogger logger;
    private ApplicationEventBus bus;
    private MessagesViewer viewer;
    private JList<?> list;

    @BeforeEach
    void setUp() throws Exception {
        BeanFactory.reset();
        configureSettings();
        BeanFactory.overrideInstance(MessageListModel.class, model);
        bus = BeanFactory.instance(ApplicationEventBus.class);
        logger = createRecordingLogger();
        model.addListDataListener(new EdtCheckingListener());
        SwingUtilities.invokeAndWait(this::createViewer);
    }

    private void configureSettings() {
        setting(AddTimestampSetting.class, false);
        setting(EscapeCharsSetting.class, false);
        setting(AutoscrollSetting.class, false);
        setting(FontNameSetting.class, "Monospaced");
        setting(FontSizeSetting.class, 12);
    }

    private OutputLogger createRecordingLogger() {
        OutputLogger logger = mock(OutputLogger.class);
        doAnswer(invocation -> {
            List<Message> batch = (List<Message>) invocation.getArguments()[0];
            logged.addAll(batch);
            return null;
        }).when(logger).log(anyList());
        return logger;
    }

    private void createViewer() {
        viewer = new MessagesViewer(logger);
        list = (JList<?>) ((JScrollPane) viewer.getComponent()).getViewport().getView();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (viewer != null) {
            viewer.shutdown();
        }
        SwingUtilities.invokeAndWait(() -> {
        });
        assertThat(offEdtModelChange.get()).isFalse();
        BeanFactory.reset();
    }

    @Test
    void shouldDisplayAndLogSerialTailBeforeCloseMessageWithOriginalTimestamp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            //given
            viewer.addSerialLog("partial", Instant.ofEpochMilli(1000));
            viewer.flushQueueToModel();
            assertThat(logged).isEmpty();

            //when
            viewer.addMessage(Message.info("Close serial"));
            viewer.flushQueueToModel();

            //then
            assertThat(model.getSize()).isEqualTo(2);
            assertThat(model.getElementAt(0).getText()).isEqualTo("partial");
            assertThat(model.getElementAt(0).getTimestamp()).isEqualTo(1000);
            assertThat(model.getElementAt(1).getText()).isEqualTo("Close serial");
            assertThat(logged).extracting(Message::getText).containsExactly("partial", "Close serial");
        });
    }

    @Test
    void shouldKeepMessagesAddedAfterClearEvenWhenFlushWasAlreadyScheduled() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            //given
            viewer.addSerialLog("old\n", Instant.now());
            viewer.flushQueueToModel();
            SwingUtilities.invokeLater(viewer::flushQueueToModel);

            //when
            viewer.clear();
            viewer.addMessage(Message.info("Output cleared"));
            viewer.addSerialLog("new\n", Instant.now());
        });
        SwingUtilities.invokeAndWait(() -> {
            viewer.flushQueueToModel();

            //then
            assertThat(model.getSize()).isEqualTo(2);
            assertThat(model.getElementAt(0).getText()).isEqualTo("Output cleared");
            assertThat(model.getElementAt(1).getText()).isEqualTo("new");
        });
    }

    @Test
    void shouldUseCurrentSettingsForCopyAndApplyBackgroundFontChangesOnEdt() throws Exception {
        //given
        AtomicBoolean offEdtFontChange = new AtomicBoolean();
        SwingUtilities.invokeAndWait(() -> {
            viewer.addSerialLog("A\u0001\n", Instant.ofEpochMilli(1000));
            viewer.addMessage(Message.info("Internal\u0001"));
            viewer.flushQueueToModel();
            list.setSelectionInterval(0, 1);
            assertThat(viewer.selectedLogsText()).isEqualTo("A\u0001" + System.lineSeparator() + "Internal\u0001");
            list.addPropertyChangeListener("font", e ->
                    offEdtFontChange.set(!SwingUtilities.isEventDispatchThread()));
        });

        //when
        viewer.handleEvent(setting(AddTimestampSetting.class, true));
        viewer.handleEvent(setting(EscapeCharsSetting.class, true));
        bus.post(new FontChangedEvent("Monospaced", 18));

        SwingUtilities.invokeAndWait(() -> {
            //then
            assertThat(viewer.selectedLogsText()).isEqualTo(
                    model.getElementAt(0).getFormattedTimestamp() + " A<01>" + System.lineSeparator()
                            + model.getElementAt(1).getFormattedTimestamp() + " Internal\u0001");
            assertThat(list.getFont().getSize()).isEqualTo(18);
            assertThat(offEdtFontChange.get()).isFalse();
        });
    }

    @Test
    void shouldSavePendingDataOnShutdownWithoutWaitingForBlockedEdt() throws Exception {
        //given
        SwingUtilities.invokeAndWait(() -> {
            viewer.addSerialLog("partial", Instant.ofEpochMilli(1000));
            viewer.flushQueueToModel();
        });
        CountDownLatch blocked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            blocked.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(blocked.await(2, TimeUnit.SECONDS));
        try {
            //when
            viewer.addSerialLog(" tail", Instant.ofEpochMilli(9000));
            viewer.shutdown();
            viewer.shutdown();

            //then
            assertThat(logged).extracting(Message::getText).containsExactly("partial tail");
            assertThat(logged.get(0).getTimestamp()).isEqualTo(1000);
            assertThat(offEdtModelChange.get()).isFalse();
        } finally {
            release.countDown();
        }
    }

    @Test
    void shouldDrainAfterSourceClosesAndBeforeLoggerShutsDown() {
        //given
        bus.register(new Object() {
            @Subscribe
            public void closeSource(ApplicationClosingEvent event) {
                viewer.addSerialLog("last bytes", Instant.ofEpochMilli(1000));
                viewer.addMessage(Message.info("Close serial"));
            }
        });
        bus.register(new Object() {
            @Subscribe(priority = 20)
            public void closeLogger(ApplicationClosingEvent event) {
                logger.shutdown();
            }
        });

        //when
        bus.post(new ApplicationClosingEvent());

        //then
        assertThat(logged).extracting(Message::getText).containsExactly("last bytes", "Close serial");
        org.mockito.InOrder order = inOrder(logger);
        order.verify(logger, atLeastOnce()).log(anyList());
        order.verify(logger).shutdown();
    }

    private <T, S extends AppSetting<T>> S setting(Class<S> type, T value) {
        S setting = mock(type);
        when(setting.get()).thenReturn(value);
        BeanFactory.overrideInstance(type, setting);
        return setting;
    }

    private class EdtCheckingListener implements ListDataListener {
        @Override
        public void intervalAdded(ListDataEvent e) {
            checkThread();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            checkThread();
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            checkThread();
        }

        private void checkThread() {
            if (!SwingUtilities.isEventDispatchThread()) {
                offEdtModelChange.set(true);
            }
        }
    }
}
