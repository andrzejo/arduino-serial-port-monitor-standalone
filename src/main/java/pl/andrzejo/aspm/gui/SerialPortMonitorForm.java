package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.App;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiExecuteCommand;
import pl.andrzejo.aspm.eventbus.events.app.ApplicationClosingEvent;
import pl.andrzejo.aspm.eventbus.events.app.ApplicationStartedEvent;
import pl.andrzejo.aspm.eventbus.events.command.CommandExecutedEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceCloseEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceErrorEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceOpenEvent;
import pl.andrzejo.aspm.eventbus.events.gui.ClearMonitorOutputEvent;
import pl.andrzejo.aspm.eventbus.events.serial.SerialMessageReceivedEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.factory.ObjectFactory;
import pl.andrzejo.aspm.gui.viewer.SerialViewerColored;
import pl.andrzejo.aspm.gui.viewer.Text;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.monitor.WindowPositionSetting;
import pl.andrzejo.aspm.utils.Images;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static org.apache.commons.lang.StringUtils.isBlank;
import static pl.andrzejo.aspm.factory.ObjectFactory.instance;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.*;

public class SerialPortMonitorForm {
    private final JFrame mainFrame;
    private final SerialViewerColored viewer;
    private JLabel statusLabel;

    public SerialPortMonitorForm() {
        WindowPositionSetting sizeSetting = AppSettingsFactory.create(WindowPositionSetting.class);

        mainFrame = new JFrame(App.Name);
        mainFrame.setIconImage(Images.fromResource("images/icon.png"));
        DeviceSelectorPanel deviceSelector = new DeviceSelectorPanel();
        SendCommandPanel sendCommandPanel = new SendCommandPanel();

        viewer = new SerialViewerColored(new OutputLogger());

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame.getContentPane().add(deviceSelector, BorderLayout.NORTH);
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(viewer.getComponent(), BorderLayout.CENTER);
        centerPanel.add(new MonitorRightPanel(), BorderLayout.EAST);

        mainFrame.getContentPane().add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(sendCommandPanel, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel();
        setupStatusPanel(statusPanel);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        mainFrame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        mainFrame.pack();
        Rectangle r = sizeSetting.get();
        mainFrame.setBounds(r);
        mainFrame.addComponentListener(handleMoved(e -> sizeSetting.set(mainFrame.getBounds())));
        mainFrame.addWindowListener(handleWindowClosed((e) -> instance(ApplicationEventBus.class).post(new ApplicationClosingEvent())));

        ApplicationEventBus eventBus = instance(ApplicationEventBus.class);
        eventBus.register(this);
        MainWindowContainer.setMainWindowComponent(mainFrame);
        eventBus.post(new ApplicationStartedEvent());
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

    private void addText(String text) {
        viewer.appendText(Text.appMessage(text));
    }

    private void addText(Text text) {
        viewer.appendText(text);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(CommandExecutedEvent event) {
        addText(Text.info("Execute command: [" + event.getCommand() + "]"));
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(SerialMessageReceivedEvent event) {
        addText(Text.message(event.getValue(), event.getDate()));
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
        addText(Text.error("Serial error: " + event.getMessage()));
        setStatus("Device error");
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ApiExecuteCommand event) {
        String body = isBlank(event.getBody()) ? "" : " - " + event.getBody();
        addText(Text.info("Remote command: " + event.getCommand() + body));
    }

    public void show() {
        mainFrame.setVisible(true);
    }
}
