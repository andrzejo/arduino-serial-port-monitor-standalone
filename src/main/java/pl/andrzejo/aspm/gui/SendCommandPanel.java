package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.command.ExecuteCommandEvent;
import pl.andrzejo.aspm.gui.setting.LineEndingSettingHandler;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.monitor.LineEndingSetting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;

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
        sendBtn.addActionListener(handleAction(this::executeCommand));

        add(lePanel, BorderLayout.WEST);
        add(commandPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.EAST);
        LineEndingSettingHandler handler = new LineEndingSettingHandler(AppSettingsFactory.create(LineEndingSetting.class));
        handler.setupComponent(lineEndingComboBox);
        ApplicationEventBus.instance().register(this);
    }

    private void executeCommand(ActionEvent actionEvent) {
        String command = commandEdit.getText();
        String lineEnding = getLineEnding();
        ApplicationEventBus.instance().post(new ExecuteCommandEvent(command, lineEnding));
    }

    private String getLineEnding() {
        return "\n";
    }
}
