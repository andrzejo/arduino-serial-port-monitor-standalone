package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.gui.ClearMonitorOutputEvent;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.SaveLogToFile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;

public class MonitorRightPanel extends ContentPanel {

    public MonitorRightPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(0, 10, 0, 10));

        JButton clear = new JButton("Clear output");
        add(clear, BorderLayout.NORTH);
        clear.addActionListener(handleAction((e) -> ApplicationEventBus.instance().post(new ClearMonitorOutputEvent())));

        JCheckBox saveToFile = new JCheckBox("Save log to file");
        handleCheckboxSetting(saveToFile, AppSettingsFactory.create(SaveLogToFile.class));
        add(saveToFile, BorderLayout.SOUTH);
    }
}
