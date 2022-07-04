package pl.andrzejo.aspm.gui.viewer.color;

import pl.andrzejo.aspm.gui.viewer.SerialViewer;
import pl.andrzejo.aspm.gui.viewer.Text;
import pl.andrzejo.aspm.utils.Files;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class SerialViewerColored implements SerialViewer {
    private final JScrollPane scroll;
    private final JEditorPane editor;
    private final HTMLEditorKit kit;
    private final HTMLDocument doc;
    private final ColorFormatter formatter;
    private boolean isAutoScroll;
    private boolean isAddTimestamp;

    public SerialViewerColored() {
        formatter = new ColorFormatter();
        kit = new HTMLEditorKit();

        editor = new JEditorPane();
        editor.setEditable(false);
        scroll = new JScrollPane(editor);

        editor.setEditorKit(kit);
        doc = (HTMLDocument) kit.createDefaultDocument();
        editor.setDocument(doc);
        editor.setBackground(SystemColor.window);

        StyleSheet docStyles = doc.getStyleSheet();
        StyleSheet appStyles = loadCss();
        docStyles.addStyleSheet(appStyles);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

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

    private StyleSheet loadCss() {
        StyleSheet s = new StyleSheet();
        try {
            s.loadRules(new FileReader(Files.fromResource("styles/def.css")), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return s;
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
        editor.setText("");
        appendText(Text.message("Output cleared", new Date()));
    }

    @Override
    public void appendText(Text text) {
        appendText(formatter.format(text, isAddTimestamp));
    }

    public void appendText(String text) {
        try {
            kit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
            scrollToEnd();
            // Files.write("C:/tmp/ard.html", editor.getText());
        } catch (BadLocationException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void scrollToEnd() {
        if (isAutoScroll) {
            JScrollBar bar = scroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        }
    }
}
