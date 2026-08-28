/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui.viewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.device.DeviceCloseEvent;
import pl.andrzejo.aspm.eventbus.events.gui.FontChangedEvent;
import pl.andrzejo.aspm.eventbus.events.gui.GetMonitorOutputEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.gui.OutputLogger;
import pl.andrzejo.aspm.gui.viewer.util.TimestampHelper;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static javax.swing.SwingUtilities.invokeLater;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;
import static pl.andrzejo.aspm.gui.viewer.Styles.MessageType.*;
import static pl.andrzejo.aspm.settings.appsettings.AppSettingGetter.get;

public class SerialViewerColored {
    private static final int MAX_DOCUMENT_LENGTH = 200_000;
    private static final int TARGET_DOCUMENT_LENGTH = 150_000;
    private static final int MAX_TEXTS_PER_FLUSH = 2_000;
    private static final int MAX_CHARS_PER_FLUSH = 20_000;
    private static final Logger log = LoggerFactory.getLogger(SerialViewerColored.class);
    private final JScrollPane scroll;

    private final StyledDocument doc;
    private final Styles styles;
    private final JTextPane editor;
    private final SerialMessageType serialMessageType;
    private boolean isAutoScroll = get(AutoscrollSetting.class);
    private boolean isAddTimestamp = get(AddTimestampSetting.class);
    private boolean isEscapeChars = get(EscapeCharsSetting.class);
    private final OutputLogger logger;
    private final StringBuilder serialOutput = new StringBuilder();
    private final Queue<PendingText> pendingTexts = new ConcurrentLinkedQueue<>();
    private final Object pendingTextsLock = new Object();
    private final Timer flushTimer;
    private char lastChar = '\n';

    public SerialViewerColored(OutputLogger logger) {
        serialMessageType = instance(SerialMessageType.class);
        this.logger = logger;
        editor = new JTextPane();
        editor.setEditable(false);
        doc = editor.getStyledDocument();
        scroll = new JScrollPane(editor);
        setFont(AppSettingsFactory.create(FontNameSetting.class).get(), AppSettingsFactory.create(FontSizeSetting.class).get());
        styles = new Styles(doc);
        editor.setBackground(SystemColor.window);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        flushTimer = new Timer(50, e -> flushPendingText());
        flushTimer.setCoalesce(true);
        flushTimer.start();
        instance(ApplicationEventBus.class).register(this);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(AutoscrollSetting event) {
        isAutoScroll = event.get();
        DefaultCaret caret = (DefaultCaret) editor.getCaret();
        caret.setUpdatePolicy(isAutoScroll ? DefaultCaret.ALWAYS_UPDATE : DefaultCaret.NEVER_UPDATE);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(AddTimestampSetting event) {
        isAddTimestamp = event.get();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(EscapeCharsSetting event) {
        isEscapeChars = event.get();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(FontChangedEvent event) {
        setFont(event.getName(), event.getSize());
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceCloseEvent event) {
        pendingTexts.clear();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public String handleEvent(GetMonitorOutputEvent event) {
        if (event.isWithMessages()) {
            return getCurrentText();
        }
        return serialOutput.toString();
    }

    public JComponent getComponent() {
        return scroll;
    }

    public void clear() {
        invokeLater(() -> {
            try {
                pendingTexts.clear();
                serialOutput.setLength(0);
                doc.remove(0, doc.getLength());
            } catch (BadLocationException e) {
                //ignore
            }
        });
    }

    public void appendText(Text text) {
        if (isAddTimestamp) {
            boolean isNewLineEnded = lastChar == '\n' || lastChar == '\r';
            String prefix = isNewLineEnded ? "" : "\n";
            insertText(prefix + TimestampHelper.getTimestamp(text.getDate()) + ": ", styles.get(TIME));
        }

        switch (text.getType()) {
            case SERIAL_MESSAGE:
                serialOutput.append(text.getText());
                formatSerialMessage(text.getText());
                break;

            case INTERNAL_MESSAGE:
                insertText(String.format(" \uD83D\uDEC8 %s \n", text.getText()), styles.get(INTERNAL_MESSAGE));
                break;

            case INTERNAL_INFO:
                insertText(String.format(" \uD83D\uDEC8 %s \n", text.getText()), styles.get(INTERNAL_INFO));
                break;

            case INTERNAL_ERROR:
                insertText(String.format(" \u26A0 %s \n", text.getText()), styles.get(INTERNAL_ERROR));
                break;
            default:
                throw new ColorFormatterException("Unsupported text type: " + text.getType().name());
        }
    }

    private void flushPendingText() {
        List<PendingText> snapshot = pollPendingTexts();
        if (snapshot.isEmpty()) {
            return;
        }
        log.info("flushing pending {} texts, doc_len: {}", snapshot.size(), doc.getLength());

        List<PendingText> grouped = groupByStyle(snapshot);
        trimDocumentBeforeInsert(grouped);

        try {
            StringBuilder currentText = new StringBuilder();
            for (PendingText text : grouped) {
                currentText.append(text.text);
                doc.insertString(doc.getLength(), text.text, text.style);
            }
            trimDocument();
            scrollDown();
            logger.log(currentText.toString());
        } catch (BadLocationException e) {
            throw new RuntimeException("Cannot append serial output", e);
        }
    }

    private String escapeText(String text) {
        StringBuilder sb = new StringBuilder();
        for (byte b : text.getBytes()) {
            int c = b & 0xFF;
            if ((c >= 32 && c < 127) || c == 10 || c == 13) {
                sb.append((char) c);
            } else {
                sb.append(String.format("<%02X>", c));
            }
        }
        return sb.toString();
    }

    private List<PendingText> pollPendingTexts() {
        List<PendingText> snapshot = new ArrayList<>();

        int textCount = 0;
        int charCount = 0;

        PendingText pending;

        while (textCount < MAX_TEXTS_PER_FLUSH
                && charCount < MAX_CHARS_PER_FLUSH
                && (pending = pendingTexts.poll()) != null) {

            snapshot.add(pending);
            textCount++;
            charCount += pending.text.length();
        }

        return snapshot;
    }

    private List<PendingText> groupByStyle(List<PendingText> texts) {
        if (texts.isEmpty()) {
            return Collections.emptyList();
        }

        List<PendingText> grouped = new ArrayList<>();

        Style currentStyle = texts.get(0).style;
        StringBuilder currentText = new StringBuilder(texts.get(0).text);

        for (int i = 1; i < texts.size(); i++) {
            PendingText next = texts.get(i);
            if (currentStyle == next.style) {
                currentText.append(next.text);
            } else {
                grouped.add(new PendingText(currentText.toString(), currentStyle));
                currentStyle = next.style;
                currentText.setLength(0);
                currentText.append(next.text);
            }
        }
        grouped.add(new PendingText(currentText.toString(), currentStyle));
        return grouped;
    }

    private void trimDocumentBeforeInsert(List<PendingText> texts) {
        int incomingLength = 0;

        for (PendingText text : texts) {
            incomingLength += text.text.length();
        }

        int expectedLength = doc.getLength() + incomingLength;

        if (expectedLength <= MAX_DOCUMENT_LENGTH) {
            return;
        }
        int charsToRemove = expectedLength - TARGET_DOCUMENT_LENGTH;
        charsToRemove = Math.min(charsToRemove, doc.getLength());
        try {
            doc.remove(0, charsToRemove);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private void setFont(String name, Integer size) {
        Font font = editor.getFont();
        Font font1 = new Font(name, font.getStyle(), size);
        editor.setFont(font1);
    }

    private void formatSerialMessage(String text) {
        Styles.MessageType type = serialMessageType.getType(text);
        String escaped = isEscapeChars ? escapeText(text) : text;
        insertText(escaped, styles.get(type));
    }

    private String getCurrentText() {
        try {
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            return "";
        }
    }

    private void scrollDown() {
        if (isAutoScroll) {
            BoundedRangeModel model = scroll.getVerticalScrollBar().getModel();
            int maximum = model.getMaximum();
            int extent = model.getExtent();
            model.setValue(maximum - extent);
        }

    }

    private void insertText(String text, Style style) {
        synchronized (pendingTextsLock) {
            lastChar = text.charAt(text.length() - 1);
            pendingTexts.add(new PendingText(text, style));
        }
    }

    private void trimDocument() {
        int overflow = doc.getLength() - MAX_DOCUMENT_LENGTH;

        if (overflow > 0) {
            try {
                doc.remove(0, overflow);
            } catch (BadLocationException e) {
                //
            }
        }
    }

    private static class PendingText {
        final String text;
        final Style style;

        PendingText(String text, Style style) {
            this.text = text;
            this.style = style;
        }
    }
}

