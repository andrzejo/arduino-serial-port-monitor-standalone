package pl.andrzejo.aspm.gui;

import com.google.common.eventbus.Subscribe;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.settings.appsettings.AutoscrollSetting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;

public class SendCommandPanel extends ContentPanel {
    JComboBox<String> lineEndingComboBox = new JComboBox<>();
    JEditorPane commandEdit = new JEditorPane();
    JButton sendBtn = new JButton("Send");

    public SendCommandPanel() {
        setPreferredComboBoxSize(lineEndingComboBox);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(1, 5, 15, 5));
        JPanel lePanel = createLabeled("Line ending:", lineEndingComboBox);
        JPanel commandPanel = createLabeled("Command:", commandEdit);
        JPanel btnPanel = createLabeled("", sendBtn);
        add(lePanel, BorderLayout.WEST);
        add(commandPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.EAST);

        ApplicationEventBus.instance().register(this);
    }
}
