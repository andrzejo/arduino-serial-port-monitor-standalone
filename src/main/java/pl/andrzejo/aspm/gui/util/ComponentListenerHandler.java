package pl.andrzejo.aspm.gui.util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class ComponentListenerHandler {

    public static AbstractAction handleAction(Consumer<ActionEvent> handler) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handler.accept(e);
            }
        };
    }

    public static DocumentListener handleDocumentChange(Consumer<DocumentEvent> handler) {
        return new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                handler.accept(e);
            }

            public void removeUpdate(DocumentEvent e) {
                handler.accept(e);
            }

            public void insertUpdate(DocumentEvent e) {
                handler.accept(e);
            }
        };
    }

    public static MouseAdapter mouseClicked(Consumer<MouseEvent> handler) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handler.accept(e);
            }
        };
    }
}
