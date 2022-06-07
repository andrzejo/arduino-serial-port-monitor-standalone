package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.settings.SettingsRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class SerialPortMonitorForm {
    public static final String SR_GUI_MAIN_RECT = "gui.main.rect";
    private final JFrame mainFrame;
    private final JTextArea textArea;
    private JScrollPane scroll;

    public SerialPortMonitorForm() {
        mainFrame = new JFrame("Arduino Serial Port Monitor - Standalone");
        JEditorPane jep = new JEditorPane();
        jep.setEditable(false);
        jep.setContentType("text/html");
        jep.setText("<html>Could not load webpage</html>");

        textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        scroll = new JScrollPane(jep);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Label emptyLabel = new Label("Editor");
        mainFrame.getContentPane().add(emptyLabel, BorderLayout.NORTH);
        mainFrame.getContentPane().add(scroll, BorderLayout.CENTER);
        mainFrame.pack();
        mainFrame.setBounds(SettingsRepository.instance().getRect(SR_GUI_MAIN_RECT, new Rectangle(10, 10, 400, 400)));
        mainFrame.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                SettingsRepository.instance().saveRect(SR_GUI_MAIN_RECT, mainFrame.getBounds());
            }
        });
        setupTextArea();
    }

    private void setupTextArea() {

    }

    public void show() {
        mainFrame.setVisible(true);
    }
}
