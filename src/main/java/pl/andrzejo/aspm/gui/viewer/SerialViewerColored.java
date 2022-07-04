package pl.andrzejo.aspm.gui.viewer;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.gui.viewer.util.TimestampHelper;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.AddTimestampSetting;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.AutoscrollSetting;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.awt.*;

import static pl.andrzejo.aspm.gui.viewer.Styles.MessageType.*;
import static pl.andrzejo.aspm.settings.appsettings.AppSettingGetter.get;

public class SerialViewerColored {
    private final JScrollPane scroll;
    private final StyledDocument doc;
    private final Styles styles;
    private boolean isAutoScroll = get(AutoscrollSetting.class);
    private boolean isAddTimestamp = get(AddTimestampSetting.class);

    public SerialViewerColored() {
        JTextPane editor = new JTextPane();
        doc = editor.getStyledDocument();
        scroll = new JScrollPane(editor);
        Font font = editor.getFont();
        Font font1 = new Font("monospaced", font.getStyle(), font.getSize());
        editor.setFont(font1);
        styles = new Styles(doc);
        editor.setBackground(SystemColor.window);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        ApplicationEventBus.instance().register(this);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(AutoscrollSetting event) {
        isAutoScroll = event.get();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(AddTimestampSetting event) {
        isAddTimestamp = event.get();
    }


    public JComponent getComponent() {
        return scroll;
    }

    public void clear() {

    }

    public void appendText(Text text) {
        if (isAddTimestamp) {
            insertText(TimestampHelper.getTimestamp(text.getDate()) + ": ", styles.get(TIME));
        }

        switch (text.getType()) {
            case SERIAL_MESSAGE:
                insertText(text.getText(), styles.get(SERIAL_MESSAGE));
                break;

            case INTERNAL_MESSAGE:
                insertText(String.format(" \uD83D\uDEC8 %s \n", text.getText()), styles.get(INTERNAL_MESSAGE));
                break;

            case INTERNAL_ERROR:
                insertText(text.getText(), styles.get(INTERNAL_ERROR));
                break;
            default:
                throw new ColorFormatterException("Unsupported text type: " + text.getType().name());
        }
        scrollDown();
    }

    private void scrollDown() {
        if (isAutoScroll) {
            BoundedRangeModel model = scroll.getVerticalScrollBar().getModel();
            model.setValue(model.getMaximum() - model.getExtent());
        }
    }

    private void insertText(String text, Style style) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}

