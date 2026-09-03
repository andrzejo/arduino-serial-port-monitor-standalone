/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.App;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiExecuteCommand;
import pl.andrzejo.aspm.eventbus.events.app.ApplicationClosingEvent;
import pl.andrzejo.aspm.eventbus.events.command.CommandExecutedEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceCloseEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceErrorEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceOpenEvent;
import pl.andrzejo.aspm.eventbus.events.gui.BringWindowToTopEvent;
import pl.andrzejo.aspm.eventbus.events.gui.ClearMonitorOutputEvent;
import pl.andrzejo.aspm.eventbus.events.gui.WindowAlwaysOnTopEvent;
import pl.andrzejo.aspm.eventbus.events.serial.SerialMessageReceivedEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.factory.BeanFactory;
import pl.andrzejo.aspm.gui.cmd.SendCommandPanel;
import pl.andrzejo.aspm.gui.viewer.MessagesViewer;
import pl.andrzejo.aspm.gui.viewer.model.Message;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.monitor.WindowPositionSetting;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.WindowAlwaysOnTopSetting;
import pl.andrzejo.aspm.utils.Images;
import pl.andrzejo.aspm.utils.Sleeper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.EventQueue.invokeLater;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.*;

public class SerialPortMonitorForm {
    private final JFrame mainFrame;
    private final MessagesViewer viewer;
    private final AtomicBoolean isCleanedUp = new AtomicBoolean(false);
    private JLabel statusLabel;

    public SerialPortMonitorForm() {
        WindowPositionSetting sizeSetting = AppSettingsFactory.create(WindowPositionSetting.class);
        WindowAlwaysOnTopSetting alwaysOnTop = AppSettingsFactory.create(WindowAlwaysOnTopSetting.class);
        ApplicationEventBus eventBus = instance(ApplicationEventBus.class);
        eventBus.register(this);

        mainFrame = new JFrame(App.Name);
        mainFrame.setIconImage(Images.fromResource("images/icon.png"));
        DeviceSelectorPanel deviceSelector = new DeviceSelectorPanel();
        SendCommandPanel sendCommandPanel = new SendCommandPanel();

        viewer = new MessagesViewer(BeanFactory.instance(OutputLogger.class));

        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        mainFrame.getContentPane().add(deviceSelector, BorderLayout.NORTH);
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());

        centerPanel.add(viewer.getComponent(), BorderLayout.CENTER);

        centerPanel.add(new MonitorSettingsPanel(), BorderLayout.SOUTH);

        mainFrame.getContentPane().add(centerPanel, BorderLayout.CENTER);
        mainFrame.getContentPane().add(sendCommandPanel, BorderLayout.EAST);

        JPanel statusPanel = new JPanel();
        setupStatusPanel(statusPanel);

        mainFrame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
        mainFrame.pack();
        Rectangle r = sizeSetting.get();
        mainFrame.setBounds(r);
        mainFrame.addComponentListener(handleMoved(e -> sizeSetting.set(mainFrame.getBounds())));
        mainFrame.addWindowListener(handleWindowClosed((e) -> applicationOnExitCleanup()));

        mainFrame.setAlwaysOnTop(alwaysOnTop.get());

        MainWindowContainer.setMainWindowComponent(mainFrame);
        Runtime.getRuntime().addShutdownHook(new Thread(this::applicationOnExitCleanup, "Shutdown-Hook-Thread"));
    }

    private void applicationOnExitCleanup() {
        if (isCleanedUp.compareAndSet(false, true)) {
            instance(ApplicationEventBus.class).post(new ApplicationClosingEvent());
            mainFrame.dispose();
            System.exit(0);
        }
    }

    private void setupStatusPanel(JPanel statusPanel) {
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(0, 4, 4, 4));
        contentPanel.setLayout(new BorderLayout());

        JLabel about = new JLabel("about app");
        statusLabel = new JLabel();

        contentPanel.add(about, BorderLayout.EAST);
        contentPanel.add(statusLabel, BorderLayout.WEST);

        about.setForeground(Color.BLUE);
        about.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(contentPanel, BorderLayout.CENTER);
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SystemColor.activeCaptionBorder));

        about.addMouseListener(mouseClicked((e) -> new AboutForm(mainFrame).showModal()));
    }

    private void addText(String msg) {
        viewer.addMessage(Message.info(msg));
    }

    private void addText(Message msg) {
        viewer.addMessage(msg);
    }

    private void addSerialLog(String msg, Instant date) {
        viewer.addSerialLog(msg, date);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(CommandExecutedEvent event) {
        String ending = decodedEnding(event.getLineEnding());
        String desc = event.getCommand().getDescription();
        if (isNotBlank(desc)) {
            desc = "(" + desc + ") ";
        }
        addText(Message.info("Execute command: " + desc + "[" + event.getCommand().getCommand() + ending + "]"));
    }

    private String decodedEnding(String lineEnding) {
        if (lineEnding == null || lineEnding.isEmpty()) {
            return "";
        }
        return lineEnding
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(SerialMessageReceivedEvent event) {
        addSerialLog(event.getValue(), event.getDate());
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ClearMonitorOutputEvent event) {
        viewer.clear();
        addText("Output cleared");
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceCloseEvent event) {
        addText("Close serial: " + event.getConfig().getDevice());
        setStatus("Device is closed");
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceOpenEvent event) {
        addText("Open serial: " + event.getConfig().getDevice());
        setStatus("Device is open: " + event.getConfig().getDevice());
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceErrorEvent event) {
        addText(Message.error("Serial error: " + event.getMessage()));
        setStatus("Device error");
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ApiExecuteCommand event) {
        String body = isBlank(event.getBody()) ? "" : " - " + event.getBody();
        addText(Message.info("Remote command: " + event.getCommand() + body));
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(BringWindowToTopEvent event) {
        invokeLater(() -> {
            if (event.isBlur()) {
                mainFrame.toBack();
            } else {
                mainFrame.setState(java.awt.Frame.ICONIFIED);
            }

        });

        Sleeper.sleep(500);

        invokeLater(() -> {
            if (!event.isBlur()) {
                mainFrame.setState(java.awt.Frame.NORMAL);
            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(WindowAlwaysOnTopEvent event) {
        invokeLater(() -> mainFrame.setAlwaysOnTop(event.isAlwaysOnTop()));
    }


    public void show() {
        mainFrame.setVisible(true);
    }
}
