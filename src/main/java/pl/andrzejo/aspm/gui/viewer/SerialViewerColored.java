package pl.andrzejo.aspm.gui.viewer;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.io.IOException;

public class SerialViewerColored implements SerialViewer {
    private final JScrollPane scroll;
    private JEditorPane editor;
    private HTMLEditorKit kit;
    private HTMLDocument doc;


    public SerialViewerColored() {
        editor = new JEditorPane("text/html", "");
        editor.setEditable(false);
        scroll = new JScrollPane(editor);
        kit = new HTMLEditorKit();
        doc = new HTMLDocument();
        editor.setEditorKit(kit);
        editor.setDocument(doc);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    }

    @Override
    public void setAutoScroll(boolean auto) {

    }

    @Override
    public void setAddTimestamps(boolean auto) {

    }

    @Override
    public JComponent getComponent() {
        return scroll;
    }

    @Override
    public void clear() {
        editor.setText("<html><head> <body bgcolor='red'> ");
    }

    @Override
    public void appendText(Text text) {
        try {
            kit.insertHTML(doc, doc.getLength(), "<font color='red'>" + text.getText() + "</font>", 0, 0, null);
        } catch (BadLocationException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
