/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.app.ApplicationClosingEvent;
import pl.andrzejo.aspm.eventbus.events.gui.FontChangedEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.gui.OutputLogger;
import pl.andrzejo.aspm.gui.viewer.model.Message;
import pl.andrzejo.aspm.gui.viewer.model.MessageListModel;
import pl.andrzejo.aspm.settings.appsettings.AppSettingGetter;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;
import static pl.andrzejo.aspm.settings.appsettings.AppSettingGetter.get;

public class MessagesViewer {
    private static final int FLUSH_INTERVAL_MS = 40; // ~25 FPS
    private static final Logger log = LoggerFactory.getLogger(MessagesViewer.class);
    private final MessageListModel messagesListModel = instance(MessageListModel.class);
    private final JList<Message> messagesList = new JList<>(messagesListModel);
    private final JScrollPane scrollPane;
    private final MessageCellRenderer cellRenderer;
    private final MessageInputBuffer inputBuffer = new MessageInputBuffer(instance(SerialMessageTypeResolver.class));
    private final Timer flushTimer;
    private final OutputLogger outputLogger;
    private boolean renderTimestamps = AppSettingGetter.get(AddTimestampSetting.class);
    private boolean escapeChars = AppSettingGetter.get(EscapeCharsSetting.class);
    private boolean isAutoScroll = get(AutoscrollSetting.class);

    public MessagesViewer(OutputLogger outputLogger) {
        this.outputLogger = outputLogger;
        cellRenderer = new MessageCellRenderer();
        cellRenderer.renderTimestamp(renderTimestamps);
        cellRenderer.escapeChars(escapeChars);

        messagesList.setCellRenderer(cellRenderer);
        messagesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        messagesList.setVisibleRowCount(-1);

        scrollPane = new JScrollPane(messagesList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.getViewport().setOpaque(true);
        messagesList.setOpaque(true);
        setFont(AppSettingsFactory.create(FontNameSetting.class).get(), AppSettingsFactory.create(FontSizeSetting.class).get());
        instance(ApplicationEventBus.class).register(this);
        setupHandlers();
        flushTimer = new Timer(FLUSH_INTERVAL_MS, e -> flushQueueToModel());
        flushTimer.start();
    }

    public JComponent getComponent() {
        return scrollPane;
    }

    public void addSerialLog(String text, Instant date) {
        inputBuffer.addSerialLog(text, date);
    }

    public void addMessage(Message message) {
        inputBuffer.addMessage(message);
    }

    void flushQueueToModel() {
        MessageInputBuffer.Batch batch;
        synchronized (inputBuffer) {
            if (inputBuffer.isClosed() || !inputBuffer.hasPending()) {
                return;
            }
            batch = inputBuffer.drain();
            outputLogger.log(batch.completed);
        }
        if (inputBuffer.isClosed()) {
            return;
        }

        JScrollBar vBar = scrollPane.getVerticalScrollBar();
        int tolerance = messagesList.getFixedCellHeight() * 2;
        boolean isAtBottom = (vBar.getValue() + vBar.getVisibleAmount()) >= (vBar.getMaximum() - tolerance);

        if (batch.clearModel) {
            messagesListModel.clear();
        }
        messagesListModel.appendBatch(batch.completed, batch.incomplete);
        if (isAutoScroll && isAtBottom && messagesListModel.getSize() > 0) {
            scrollToEnd();
        }
    }

    public void clear() {
        inputBuffer.clear();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(AutoscrollSetting event) {
        boolean enabled = event.get();
        onEdt(() -> {
            isAutoScroll = enabled;
            if (enabled) {
                scrollToEnd();
            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(AddTimestampSetting event) {
        boolean enabled = event.get();
        onEdt(() -> {
            renderTimestamps = enabled;
            cellRenderer.renderTimestamp(enabled);
            repaint();
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(EscapeCharsSetting event) {
        boolean enabled = event.get();
        onEdt(() -> {
            escapeChars = enabled;
            cellRenderer.escapeChars(enabled);
            repaint();
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(FontChangedEvent event) {
        onEdt(() -> setFont(event.getName(), event.getSize()));
    }

    @Subscribe(priority = 10) // After serial input stops, before OutputLogger shuts down.
    @SuppressWarnings("unused")
    private void handleEvent(ApplicationClosingEvent event) {
        log.info("Shutting down the message queue");
        shutdown();
    }

    public void shutdown() {
        synchronized (inputBuffer) {
            inputBuffer.close();
            flushTimer.stop();
            // No Swing model updates: this also works from the JVM shutdown hook.
            while (inputBuffer.hasPending()) {
                outputLogger.log(inputBuffer.drain().completed);
            }
        }
    }

    private static void onEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    private void repaint() {
        messagesList.revalidate();
        messagesList.repaint();
    }

    private void scrollToEnd() {
        int lastIndex = messagesListModel.getSize() - 1;

        if (lastIndex >= 0) {
            messagesList.ensureIndexIsVisible(lastIndex);
        }
    }

    private void setupHandlers() {
        messagesList
                .getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copyLogs");

        messagesList
                .getActionMap()
                .put("copyLogs", handleAction(e -> copySelectedLogs()));
    }

    private void copySelectedLogs() {
        if (messagesList.isSelectionEmpty()) {
            return;
        }
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(selectedLogsText()), null);
    }

    String selectedLogsText() {
        List<Message> selected = messagesList.getSelectedValuesList();
        return selected.stream()
                .map(message -> {
                    String timestamp = renderTimestamps ? message.getFormattedTimestamp() + " " : "";
                    String text = escapeChars && !message.isInternal() ? message.getEscapedText() : message.getText();
                    return timestamp + text;
                })
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private void setFont(String name, Integer size) {
        Font font = messagesList.getFont();
        Font font1 = new Font(name, font.getStyle(), size);
        messagesList.setFont(font1);
        cellRenderer.setFont(font1);
        int height = messagesList.getFontMetrics(font1).getHeight();
        messagesList.setFixedCellHeight(height);
        scrollPane.getVerticalScrollBar().setUnitIncrement(height * 3);
        repaint();
    }

}
