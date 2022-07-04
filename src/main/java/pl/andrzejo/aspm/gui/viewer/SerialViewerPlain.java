package pl.andrzejo.aspm.gui.viewer;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;

public class SerialViewerPlain implements SerialViewer {
    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scroll;
    private boolean autoScroll;
    private boolean addTimestamps;

    public SerialViewerPlain() {
        textArea = new RSyntaxTextArea();
        textArea.setEditable(false);
        scroll = new RTextScrollPane(textArea);
    }

    @Override
    public void setAutoScroll(boolean auto) {
        autoScroll = auto;
    }

    @Override
    public void setAddTimestamps(boolean auto) {
        addTimestamps = auto;
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
        EventQueue.invokeLater(() -> {
            Document document = textArea.getDocument();
            try {
                document.insertString(document.getLength(), text.getText(), null);
                if (autoScroll) {
                    textArea.setCaretPosition(document.getLength());
                }
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
