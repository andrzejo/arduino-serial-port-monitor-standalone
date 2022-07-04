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
import pl.andrzejo.aspm.gui.viewer.SerialViewerColored;
import pl.andrzejo.aspm.gui.viewer.Text;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.monitor.WindowPositionSetting;
import pl.andrzejo.aspm.utils.Images;

import javax.swing.*;
import java.awt.*;

import static org.apache.commons.lang.StringUtils.isBlank;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleMoved;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleWindowClosed;

public class SerialPortMonitorForm {
    private final JFrame mainFrame;
    private final SerialViewerColored viewer;

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
        mainFrame.getContentPane().add(sendCommandPanel, BorderLayout.SOUTH);

        mainFrame.pack();
        Rectangle r = sizeSetting.get();
        mainFrame.setBounds(r);
        mainFrame.addComponentListener(handleMoved(e -> sizeSetting.set(mainFrame.getBounds())));
        mainFrame.addWindowListener(handleWindowClosed((e) -> ApplicationEventBus.instance().post(new ApplicationClosingEvent())));


        ApplicationEventBus eventBus = ApplicationEventBus.instance();
        eventBus.register(this);
        eventBus.post(new ApplicationStartedEvent());
        MainWindowContainer.setMainWindowComponent(mainFrame);
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
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceOpenEvent event) {
        addText("Open serial: " + event.getConfig().getDevice());
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceErrorEvent event) {
        addText(Text.error("Serial error: " + event.getMessage()));
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
