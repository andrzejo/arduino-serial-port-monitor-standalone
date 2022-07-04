package pl.andrzejo.aspm.gui;

import com.google.common.eventbus.Subscribe;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.ApplicationStartedEvent;
import pl.andrzejo.aspm.eventbus.events.SerialMessageReceivedEvent;
import pl.andrzejo.aspm.settings.appsettings.AutoscrollSetting;
import pl.andrzejo.aspm.settings.types.RectSetting;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class SerialPortMonitorForm {
    private final JFrame mainFrame;
    private final RSyntaxTextArea textArea;
    private boolean autoScroll;

    public SerialPortMonitorForm() {
        RectSetting sizeSetting = new RectSetting("gui.main.rect", new Rectangle(10, 10, 400, 400));
        mainFrame = new JFrame("Arduino Serial Port Monitor - Standalone");

        textArea = new RSyntaxTextArea();
        textArea.setEditable(false);

        DeviceSelectorPanel deviceSelector = new DeviceSelectorPanel();
        SendCommandPanel sendCommandPanel = new SendCommandPanel();

        JScrollPane scroll = new JScrollPane(textArea);
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
        ApplicationEventBus eventBus = ApplicationEventBus.instance();
        eventBus.register(this);
        eventBus.post(new ApplicationStartedEvent());
    }

    @Subscribe
    public void stringEvent(SerialMessageReceivedEvent event) {
        EventQueue.invokeLater(() -> {
            Document document = textArea.getDocument();
            try {
                document.insertString(document.getLength(), event.getValue(), null);
                if (autoScroll) {
                    textArea.setCaretPosition(document.getLength());
                }
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(event.getValue());
    }


    @Subscribe
    public void handleEvent(AutoscrollSetting event) {
        autoScroll = event.get();
    }

    public void show() {
        mainFrame.setVisible(true);
    }
}
