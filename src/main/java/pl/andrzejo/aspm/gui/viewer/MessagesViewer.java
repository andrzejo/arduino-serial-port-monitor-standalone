/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.app.ApplicationClosingEvent;
import pl.andrzejo.aspm.eventbus.events.gui.FontChangedEvent;
import pl.andrzejo.aspm.eventbus.events.gui.GetMonitorOutputEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.factory.BeanFactory;
import pl.andrzejo.aspm.gui.OutputLogger;
import pl.andrzejo.aspm.gui.viewer.model.Message;
import pl.andrzejo.aspm.gui.viewer.model.MessageListModel;
import pl.andrzejo.aspm.gui.viewer.model.MessageType;
import pl.andrzejo.aspm.settings.appsettings.AppSettingGetter;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;
import static pl.andrzejo.aspm.settings.appsettings.AppSettingGetter.get;

public class MessagesViewer {
    private static final int MAX_LINES = 20_000;
    private static final int FLUSH_INTERVAL_MS = 40; // ~25 FPS
    private static final Logger log = LoggerFactory.getLogger(MessagesViewer.class);
    private final SerialMessageTypeResolver msgTypeResolver = BeanFactory.instance(SerialMessageTypeResolver.class);
    private final MessageListModel messagesListModel = new MessageListModel(MAX_LINES);
    private final JList<Message> messagesList = new JList<>(messagesListModel);
    private final JScrollPane scrollPane;
    private final MessageCellRenderer cellRenderer;
    private final ConcurrentLinkedQueue<RawChunk> rawQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Message> directMessageQueue = new ConcurrentLinkedQueue<>();
    private final StringBuilder parseBuffer = new StringBuilder(4096);
    private final OutputLogger outputLogger;
    private final Boolean renderTimestamps = AppSettingGetter.get(AddTimestampSetting.class);
    private final Boolean escapeChars = AppSettingGetter.get(EscapeCharsSetting.class);
    private Instant lineStartTimestamp = null;
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
        setFont(AppSettingsFactory.create(FontNameSetting.class).get(), AppSettingsFactory.create(FontSizeSetting.class).get());
        instance(ApplicationEventBus.class).register(this);
        setupHandlers();
        Timer flushTimer = new Timer(FLUSH_INTERVAL_MS, e -> flushQueueToModel());
        flushTimer.start();
    }

    public JComponent getComponent() {
        return scrollPane;
    }

    public void addSerialLog(String log, Instant date) {
        rawQueue.offer(new RawChunk(date != null ? date : Instant.now(), log));
    }

    public void addMessage(Message message) {
        directMessageQueue.offer(message);
    }

    private void flushQueueToModel() {
        if (rawQueue.isEmpty() && directMessageQueue.isEmpty()) {
            return;
        }

        List<Message> completedMessages = new ArrayList<>();

        Message directMsg;
        while ((directMsg = directMessageQueue.poll()) != null) {
            completedMessages.add(directMsg);
        }

        RawChunk chunk;
        while ((chunk = rawQueue.poll()) != null) {
            String text = chunk.text;
            if (text == null || text.isEmpty()) {
                continue;
            }

            Instant chunkTime = chunk.timestamp;
            int len = text.length();
            int start = 0;

            for (int i = 0; i < len; i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    Instant msgTime = (lineStartTimestamp != null) ? lineStartTimestamp : chunkTime;
                    int end = (i > start && text.charAt(i - 1) == '\r') ? i - 1 : i;
                    parseBuffer.append(text, start, end);
                    String fullLine = parseBuffer.toString();
                    if (fullLine.endsWith("\r")) {
                        fullLine = fullLine.substring(0, fullLine.length() - 1);
                    }

                    MessageType type = msgTypeResolver.resolve(fullLine);
                    completedMessages.add(new Message(msgTime.toEpochMilli(), fullLine, type));
                    parseBuffer.setLength(0);
                    lineStartTimestamp = null;
                    start = i + 1;
                }
            }

            if (start < len) {
                if (lineStartTimestamp == null) {
                    lineStartTimestamp = chunkTime;
                }
                parseBuffer.append(text, start, len);
            }
        }

        Message incompleteMsg = null;
        if (parseBuffer.length() > 0) {
            String remainingText = parseBuffer.toString();
            Instant timestamp = lineStartTimestamp == null ? Instant.now() : lineStartTimestamp;
            incompleteMsg = new Message(timestamp.toEpochMilli(), remainingText, msgTypeResolver.resolve(remainingText));
        }

        JScrollBar vBar = scrollPane.getVerticalScrollBar();
        int tolerance = messagesList.getFixedCellHeight() * 2;
        boolean isAtBottom = (vBar.getValue() + vBar.getVisibleAmount()) >= (vBar.getMaximum() - tolerance);

        messagesListModel.appendBatch(completedMessages, incompleteMsg);
        outputLogger.log(completedMessages);

        if (isAutoScroll && isAtBottom && messagesListModel.getSize() > 0) {
            scrollToEnd();
        }
    }

    public void clear() {
        SwingUtilities.invokeLater(messagesListModel::clear);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(AutoscrollSetting event) {
        isAutoScroll = event.get();
        SwingUtilities.invokeLater(this::scrollToEnd);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(AddTimestampSetting event) {
        cellRenderer.renderTimestamp(event.get());
        repaint();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(EscapeCharsSetting event) {
        cellRenderer.escapeChars(event.get());
        repaint();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(FontChangedEvent event) {
        setFont(event.getName(), event.getSize());
    }

    @Subscribe
    @SuppressWarnings("unused")
    public String handleEvent(GetMonitorOutputEvent event) {
        StringBuilder sb = new StringBuilder();
        boolean withMessages = event.isWithMessages();
        messagesListModel.forEach(msg -> {
            if (!withMessages && msg.isInternal()) {
                return;
            }
            sb.append(msg.getFormattedTimestamp());
            sb.append(": ");
            sb.append(msg.getText());
            sb.append("\n");
        });
        return sb.toString();
    }

    @Subscribe(priority = -1)
    @SuppressWarnings("unused")
    private void handleEvent(ApplicationClosingEvent event) {
        log.info("Shutting down the message queue");
        flushQueueToModel();
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
        List<Message> selected = messagesList.getSelectedValuesList();

        if (selected.isEmpty()) {
            return;
        }

        String text = selected.stream()
                .map(message -> {
                    String stmp = renderTimestamps ? message.getFormattedTimestamp() + " " : "";
                    String msg = escapeChars ? message.getEscapedText() : message.getText();
                    return stmp + msg;
                })
                .collect(Collectors.joining(System.lineSeparator()));

        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(text), null);
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

    @RequiredArgsConstructor
    private static final class RawChunk {
        final Instant timestamp;
        final String text;
    }
}
