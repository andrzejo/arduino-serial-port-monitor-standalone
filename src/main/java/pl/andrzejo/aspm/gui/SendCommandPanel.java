package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.gui.setting.LineEndingSettingHandler;
import pl.andrzejo.aspm.settings.appsettings.LineEndingSetting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SendCommandPanel extends ContentPanel {
    JComboBox<String> lineEndingComboBox = new JComboBox<>();
    JEditorPane commandEdit = new JEditorPane();
    JButton sendBtn = new JButton("Send");

    public SendCommandPanel() {
        setPreferredWidthSize(lineEndingComboBox);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(1, 5, 15, 5));
        JPanel lePanel = createLabeled("Line ending:", lineEndingComboBox);
        JPanel commandPanel = createLabeled("Command:", commandEdit);
        JPanel btnPanel = createLabeled("", sendBtn);
        add(lePanel, BorderLayout.WEST);
        add(commandPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.EAST);
        LineEndingSettingHandler handler = new LineEndingSettingHandler(new LineEndingSetting());
        handler.setupComponent(lineEndingComboBox);
        ApplicationEventBus.instance().register(this);
    }
}
