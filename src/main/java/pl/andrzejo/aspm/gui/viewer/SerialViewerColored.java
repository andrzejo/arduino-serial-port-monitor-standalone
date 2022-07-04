package pl.andrzejo.aspm.gui.viewer;

import pl.andrzejo.aspm.gui.viewer.util.TimestampHelper;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import static pl.andrzejo.aspm.gui.viewer.Styles.MessageType.*;

public class SerialViewerColored implements SerialViewer {
    private final JTextPane editor;
    private final JScrollPane scroll;
    private final StyledDocument doc;
    private final Styles styles;

    private boolean autoScroll;
    private boolean addTimestamps;
    private boolean isAddTimestamp;
    private boolean isAutoScroll;

    public SerialViewerColored() {
        editor = new JTextPane();
        doc = editor.getStyledDocument();
        scroll = new JScrollPane(editor);
        Font font = editor.getFont();
        Font font1 = new Font("monospaced", font.getStyle(), font.getSize());
        editor.setFont(font1);
        styles = new Styles(doc);
        editor.setBackground(SystemColor.window);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        scroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            private final BoundedRangeModel model = scroll.getVerticalScrollBar().getModel();

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (isAutoScroll) {
                    model.setValue(model.getMaximum() - model.getExtent());
                }
            }
        });
    }

    @Override
    public void setAutoScroll(boolean value) {
        isAutoScroll = value;
    }

    @Override
    public void setAddTimestamps(boolean value) {
        isAddTimestamp = value;
    }

    @Override
    public JComponent getComponent() {
        return scroll;
    }

    @Override
    public void clear() {

    }

    @Override
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
    }

    private void insertText(String text, Style style) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}

