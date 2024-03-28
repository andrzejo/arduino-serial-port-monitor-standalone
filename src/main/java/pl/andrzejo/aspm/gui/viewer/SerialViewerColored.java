/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui.viewer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.gui.FontChangedEvent;
import pl.andrzejo.aspm.eventbus.events.gui.GetMonitorOutputEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.gui.OutputLogger;
import pl.andrzejo.aspm.gui.viewer.util.TimestampHelper;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.AddTimestampSetting;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.AutoscrollSetting;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.FontNameSetting;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.FontSizeSetting;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.awt.*;

import static javax.swing.SwingUtilities.invokeLater;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;
import static pl.andrzejo.aspm.gui.viewer.Styles.MessageType.*;
import static pl.andrzejo.aspm.settings.appsettings.AppSettingGetter.get;

public class SerialViewerColored {
    private static final Logger log = LoggerFactory.getLogger(SerialViewerColored.class);
    private final JScrollPane scroll;

    private final StyledDocument doc;
    private final Styles styles;
    private final JTextPane editor;
    private final SerialMessageType serialMessageType;
    private boolean isAutoScroll = get(AutoscrollSetting.class);
    private boolean isAddTimestamp = get(AddTimestampSetting.class);
    private final OutputLogger logger;
    private String serialOutput = "";

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

        instance(ApplicationEventBus.class).register(this);
    }

    private void setFont(String name, Integer size) {
        Font font = editor.getFont();
        Font font1 = new Font(name, font.getStyle(), size);
        editor.setFont(font1);
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
    public void handleEvent(FontChangedEvent event) {
        setFont(event.getName(), event.getSize());
    }

    @Subscribe
    @SuppressWarnings("unused")
    public String handleEvent(GetMonitorOutputEvent event) {
        if (event.isWithMessages()) {
            return getCurrentText();
        }
        return serialOutput;
    }

    public JComponent getComponent() {
        return scroll;
    }

    public void clear() {
        invokeLater(() -> {
            try {
                serialOutput = "";
                doc.remove(0, doc.getLength());
            } catch (BadLocationException e) {
                //ignore
            }
        });
    }

    public void appendText(Text text) {
        if (isAddTimestamp) {
            String c = getCurrentText();
            boolean isNewLineEnded = StringUtils.endsWithAny(c, new String[]{"\n", "\r"});
            String prefix = StringUtils.isBlank(c) || isNewLineEnded ? "" : "\n";
            insertText(prefix + TimestampHelper.getTimestamp(text.getDate()) + ": ", styles.get(TIME));
        }

        switch (text.getType()) {
            case SERIAL_MESSAGE:
                serialOutput += text.getText();
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
        scrollDown();
    }

    private void formatSerialMessage(String text) {
        Styles.MessageType type = serialMessageType.getType(text);
        insertText(text, styles.get(type));
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
            invokeLater(() -> {
                BoundedRangeModel model = scroll.getVerticalScrollBar().getModel();
                int maximum = model.getMaximum();
                int extent = model.getExtent();
                model.setValue(maximum - extent);
            });
        }

    }

    private void insertText(String text, Style style) {
        invokeLater(() -> {
            try {
                doc.insertString(doc.getLength(), text, style);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });
        logger.log(text);
    }
}

