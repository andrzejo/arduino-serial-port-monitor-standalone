package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.settings.RectSetting;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class SerialPortMonitorForm {
    private final JFrame mainFrame;
    private final JTextArea textArea;
    private JScrollPane scroll;
    private DeviceSelectorPanel deviceSelector;

    public SerialPortMonitorForm() {
        RectSetting sizeSetting = new RectSetting("gui.main.rect");
        mainFrame = new JFrame("Arduino Serial Port Monitor - Standalone");
        JEditorPane jep = new JEditorPane();
        jep.setEditable(false);
        jep.setContentType("text/html");
        jep.setText("<html>Could not load webpage</html>");

        deviceSelector = new DeviceSelectorPanel();

        textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        scroll = new JScrollPane(jep);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().add(deviceSelector, BorderLayout.NORTH);
        mainFrame.getContentPane().add(scroll, BorderLayout.CENTER);
        mainFrame.pack();
        mainFrame.setBounds(sizeSetting.get(new Rectangle(10, 10, 400, 400)));
        mainFrame.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                sizeSetting.set(mainFrame.getBounds());
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
