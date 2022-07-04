package pl.andrzejo.aspm.gui.viewer.color;

import pl.andrzejo.aspm.gui.viewer.SerialViewer;
import pl.andrzejo.aspm.gui.viewer.Text;
import pl.andrzejo.aspm.utils.Files;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class SerialViewerColored implements SerialViewer {
    private final JScrollPane scroll;
    private final JEditorPane editor;
    private final HTMLEditorKit kit;
    private final HTMLDocument doc;
    private final Element root;
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
        Element defaultRootElement = doc.getDefaultRootElement();
        Element body = defaultRootElement.getElement(0);
        try {
            doc.setInnerHTML(body, "<div class='main-container'></div");
            root = body.getElement(0);
        } catch (BadLocationException | IOException e) {
            throw new RuntimeException(e);
        }
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
            doc.insertBeforeEnd(root, text);
        } catch (BadLocationException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
