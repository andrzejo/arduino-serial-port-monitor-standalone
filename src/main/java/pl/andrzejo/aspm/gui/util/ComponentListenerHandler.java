package pl.andrzejo.aspm.gui.util;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.ApplicationClosingEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
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

    public static WindowListener handleWindowClosed(Consumer<WindowEvent> handler) {
        return new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                handler.accept(e);
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        };
    }

    public static ComponentAdapter handleMoved(Consumer<ComponentEvent> handler) {
        return new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                super.componentMoved(e);
                handler.accept(e);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                handler.accept(e);
            }
        };
    }
}
