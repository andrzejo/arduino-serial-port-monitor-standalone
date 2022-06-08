package pl.andrzejo.aspm.gui;

import com.google.common.eventbus.Subscribe;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.SerialMessageReceived;
import pl.andrzejo.aspm.settings.RectSetting;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class SerialPortMonitorForm {
    private final JFrame mainFrame;
    private final RSyntaxTextArea textArea;
    private JScrollPane scroll;
    private DeviceSelectorPanel deviceSelector;
    private SendCommandPanel sendCommandPanel;

    public SerialPortMonitorForm() {
        RectSetting sizeSetting = new RectSetting("gui.main.rect", new Rectangle(10, 10, 400, 400));
        mainFrame = new JFrame("Arduino Serial Port Monitor - Standalone");
        JEditorPane jep = new JEditorPane();
        jep.setEditable(false);
        jep.setContentType("text/html");
        jep.setText("<html>Could not load webpage</html>");

        textArea = new RSyntaxTextArea();
//        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        //textArea.setCodeFoldingEnabled(true);
        textArea.setEditable(false);
//        textArea.set

        deviceSelector = new DeviceSelectorPanel();

        sendCommandPanel = new SendCommandPanel();

        //textArea = new JTextArea();
        //textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        scroll = new JScrollPane(textArea);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame.getContentPane().add(deviceSelector, BorderLayout.NORTH);
        mainFrame.getContentPane().add(sp, BorderLayout.CENTER);
        mainFrame.getContentPane().add(sendCommandPanel, BorderLayout.SOUTH);

        mainFrame.pack();
        mainFrame.setBounds(sizeSetting.get());
        mainFrame.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                sizeSetting.set(mainFrame.getBounds());
            }
        });
        ApplicationEventBus.instance().register(this);
    }

    @Subscribe
    public void stringEvent(SerialMessageReceived event) {
        EventQueue.invokeLater(() -> {
            Document document = textArea.getDocument();
            try {
                document.insertString(document.getLength(), event.getValue(), null);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(event.getValue());
    }

    public void show() {
        mainFrame.setVisible(true);
    }
}
