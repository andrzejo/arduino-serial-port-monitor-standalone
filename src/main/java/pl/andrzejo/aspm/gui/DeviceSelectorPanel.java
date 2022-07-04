package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.setting.SettingsResetToDefaultEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceCloseEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceOpenEvent;
import pl.andrzejo.aspm.eventbus.events.device.ToggleDeviceStatusEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.gui.setting.device.*;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.AddTimestampSetting;
import pl.andrzejo.aspm.settings.appsettings.items.monitor.AutoOpenSetting;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.AutoscrollSetting;
import pl.andrzejo.aspm.settings.guihandlers.CheckBoxSettingsHandler;
import pl.andrzejo.aspm.settings.guihandlers.ListSettingHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;
import pl.andrzejo.aspm.utils.Images;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.mouseClicked;

public class DeviceSelectorPanel extends ContentPanel {
    private final TtyDeviceSetting setting;
    private final DeviceConfig initialConfig;
    private final DeviceConfig defConfig;
    private final JLabel statLabel;
    private final ImageIcon imageIcon;
    private final JButton openBtn;
    private final JLabel picLabel;
    private final ApplicationEventBus eventBus;
    private boolean isSettingsOpen;

    public DeviceSelectorPanel() {
        eventBus = ApplicationEventBus.instance();
        eventBus.register(this);

        setting = AppSettingsFactory.create(TtyDeviceSetting.class);
        initialConfig = setting.get();
        defConfig = DeviceConfig.defaultConfig();

        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(1, 5, 10, 5));

        openBtn = new JButton();
        JCheckBox autoOpenBox = new JCheckBox("Auto open");

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(2, 2, 15, 0));
        btnPanel.add(autoOpenBox);
        btnPanel.add(openBtn);

        JPanel dev = new JPanel();
        dev.setLayout(new GridLayout(1, 3, 15, 0));

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(2, 1, 0, 0));
        statusPanel.setBorder(new EmptyBorder(1, 5, 1, 0));

        imageIcon = new ImageIcon();
        picLabel = new JLabel(imageIcon);
        statLabel = new JLabel("");
        statusPanel.add(picLabel);
        statusPanel.add(statLabel);
        statLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusPanel.setPreferredSize(new Dimension(70, 60));

        dev.add(statusPanel);
        dev.add(btnPanel);

        setupComboBoxSetting("Device:", dev, new DeviceSettingHandler(setting, initialConfig, defConfig));
        setupComboBoxSetting("Baud:", dev, new BaudSettingHandler(setting, initialConfig, defConfig));

        JPanel line = new JPanel();
        line.add(statusPanel);
        line.add(dev);

        add(line, BorderLayout.WEST);
        JPanel boxes = new JPanel();
        boxes.setLayout(new GridLayout(2, 1));
        JCheckBox autoScroll = new JCheckBox("Auto scroll");
        JCheckBox addTimestamp = new JCheckBox("Add timestamp");

        boxes.add(autoScroll);
        boxes.add(addTimestamp);
        add(boxes);

        setupTtySettingsPanel();

        handleCheckboxSetting(autoOpenBox, AppSettingsFactory.create(AutoOpenSetting.class));
        handleCheckboxSetting(autoScroll, AppSettingsFactory.create(AutoscrollSetting.class));
        handleCheckboxSetting(addTimestamp, AppSettingsFactory.create(AddTimestampSetting.class));

        setDeviceStatus(false);
        openBtn.addActionListener(handleAction((e) -> eventBus.post(new ToggleDeviceStatusEvent())));
    }

    private void setDeviceStatus(boolean isOpen) {
        String path = isOpen ? "images/controller-on.png" : "images/controller-off.png";
        String label = isOpen ? "OPEN" : "CLOSED";
        String hint = isOpen ? "Device is open" : "Device is closed";
        String buttonAction = isOpen ? "Close" : "Open";
        BufferedImage controllerImg = Images.fromResource(path);
        imageIcon.setImage(controllerImg);
        statLabel.setText(label);
        openBtn.setText(buttonAction);
        picLabel.repaint();
        picLabel.setToolTipText(hint);
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

        add(createLabeled("", openSettingsLbl), BorderLayout.EAST);
        add(settingPanel, BorderLayout.SOUTH);

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
        eventBus.post(new SettingsResetToDefaultEvent());
    }

    private void setupCheckBoxSetting(String label, JPanel settingPanel, CheckBoxSettingsHandler handler) {
        JCheckBox box = new JCheckBox(label);
        JPanel panel = createLabeled("", box);
        settingPanel.add(panel);
        handler.setupComponent(box);
    }

    private void setupComboBoxSetting(String label, JPanel settingPanel, ListSettingHandler<?, ?> handler) {
        JComboBox<String> combo = new JComboBox<>();
        setPreferredWidthSize(combo);
        JPanel panel = createLabeled(label, combo);
        settingPanel.add(panel);
        handler.setupComponent(combo);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceOpenEvent event) {
        setDeviceStatus(true);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceCloseEvent event) {
        setDeviceStatus(false);
    }
}
