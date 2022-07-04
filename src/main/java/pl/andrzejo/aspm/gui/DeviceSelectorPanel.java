package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.SettingsResetToDefaultEvent;
import pl.andrzejo.aspm.gui.setting.*;
import pl.andrzejo.aspm.settings.appsettings.AddTimestampSetting;
import pl.andrzejo.aspm.settings.appsettings.AutoOpenSetting;
import pl.andrzejo.aspm.settings.appsettings.AutoscrollSetting;
import pl.andrzejo.aspm.settings.appsettings.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.guihandlers.CheckBoxSettingsHandler;
import pl.andrzejo.aspm.settings.guihandlers.ListSettingHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;

import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.mouseClicked;

public class DeviceSelectorPanel extends ContentPanel {
    private final TtyDeviceSetting setting;
    private final DeviceConfig initialConfig;
    private final DeviceConfig defConfig;
    private boolean isSettingsOpen;

    public DeviceSelectorPanel() {
        setting = new TtyDeviceSetting();
        initialConfig = setting.get();
        defConfig = DeviceConfig.defaultConfig();

        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(1, 5, 10, 5));

        JButton openBtn = new JButton("Open");
        JCheckBox autoOpenBox = new JCheckBox("Auto open");

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(2, 1, 15, 0));
        btnPanel.add(autoOpenBox);
        btnPanel.add(openBtn);

        JPanel dev = new JPanel();
        dev.setLayout(new GridLayout(1, 3, 15, 0));

        setupComboBoxSetting("Device:", dev, new DeviceSettingHandler(setting, initialConfig, defConfig));
        setupComboBoxSetting("Baud:", dev, new BaudSettingHandler(setting, initialConfig, defConfig));

        dev.add(btnPanel);

        add(dev, BorderLayout.WEST);
        JPanel boxes = new JPanel();
        boxes.setLayout(new GridLayout(2, 1));
        JCheckBox autoScroll = new JCheckBox("Auto scroll");
        JCheckBox addTimestamp = new JCheckBox("Add timestamp");

        boxes.add(autoScroll);
        boxes.add(addTimestamp);
        add(boxes, BorderLayout.EAST);

        setupTtySettingsPanel();

        handleCheckboxSetting(autoOpenBox, new AutoOpenSetting());
        handleCheckboxSetting(autoScroll, new AutoscrollSetting());
        handleCheckboxSetting(addTimestamp, new AddTimestampSetting());
    }

    private void setupTtySettingsPanel() {

        String[] labels = new String[]{"close device settings ▲", "open device settings ▼"};
        JLabel openSettingsLbl = new JLabel(labels[1]);
        openSettingsLbl.setForeground(Color.BLUE);
        openSettingsLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel settingPanel = new JPanel();
        settingPanel.setVisible(false);
        Border border = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        settingPanel.setBorder(border);
        settingPanel.setLayout(new BorderLayout(5, 5));

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(openSettingsLbl, BorderLayout.NORTH);
        p.add(settingPanel, BorderLayout.SOUTH);
        add(p, BorderLayout.SOUTH);

        JPanel devSettingsPanel = new JPanel();
        devSettingsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        setupComboBoxSetting("Parity:", devSettingsPanel, new ParitySettingHandler(setting, initialConfig, defConfig));
        setupComboBoxSetting("Data bits:", devSettingsPanel, new DataBitsSettingHandler(setting, initialConfig, defConfig));
        setupComboBoxSetting("Stop bits:", devSettingsPanel, new StopBitsSettingHandler(setting, initialConfig, defConfig));

        setupCheckBoxSetting("RTS", devSettingsPanel, new RtsSettingHandler(setting, initialConfig, defConfig));
        setupCheckBoxSetting("DTR", devSettingsPanel, new DtrSettingHandler(setting, initialConfig, defConfig));

        JPanel resetBtnPanel = new JPanel();
        JButton reset = new JButton("Reset");
        resetBtnPanel.add(createLabeled("", reset));

        settingPanel.add(devSettingsPanel, BorderLayout.WEST);
        settingPanel.add(resetBtnPanel, BorderLayout.EAST);

        openSettingsLbl.addMouseListener(mouseClicked((e) -> {
            isSettingsOpen = !isSettingsOpen;
            settingPanel.setVisible(isSettingsOpen);
            int index = isSettingsOpen ? 0 : 1;
            String label = labels[index];
            openSettingsLbl.setText(label);
        }));

        reset.addActionListener(handleAction((e) -> resetTtySettings()));
    }

    private void resetTtySettings() {
        ApplicationEventBus.instance().post(new SettingsResetToDefaultEvent());
    }

    private void setupCheckBoxSetting(String label, JPanel settingPanel, CheckBoxSettingsHandler handler) {
        JCheckBox box = new JCheckBox(label);
        JPanel panel = createLabeled("", box);
        settingPanel.add(panel);
        handler.setupComponent(box);
    }

    private void setupComboBoxSetting(String label, JPanel settingPanel, ListSettingHandler<?> handler) {
        JComboBox<String> combo = new JComboBox<>();
        setPreferredWidthSize(combo);
        JPanel panel = createLabeled(label, combo);
        settingPanel.add(panel);
        handler.setupComponent(combo);
    }

}
