package pl.andrzejo.aspm.gui;

import com.google.common.eventbus.Subscribe;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.ApplicationClosingEvent;
import pl.andrzejo.aspm.eventbus.events.ApplicationStartedEvent;
import pl.andrzejo.aspm.eventbus.events.SerialMessageReceivedEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceCloseEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceErrorEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceOpenEvent;
import pl.andrzejo.aspm.gui.viewer.SerialViewer;
import pl.andrzejo.aspm.gui.viewer.SerialViewerColored;
import pl.andrzejo.aspm.gui.viewer.Text;
import pl.andrzejo.aspm.settings.appsettings.AddTimestampSetting;
import pl.andrzejo.aspm.settings.appsettings.AutoscrollSetting;
import pl.andrzejo.aspm.settings.types.RectSetting;

import javax.swing.*;
import java.awt.*;

import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleMoved;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleWindowClosed;

public class SerialPortMonitorForm {
    private final JFrame mainFrame;
    private final SerialViewer viewer;

    public SerialPortMonitorForm() {
        //todo: move to app settings
        RectSetting sizeSetting = new RectSetting("gui.main.rect", new Rectangle(10, 10, 400, 400));

        mainFrame = new JFrame("Arduino Serial Port Monitor - Standalone");

        DeviceSelectorPanel deviceSelector = new DeviceSelectorPanel();
        SendCommandPanel sendCommandPanel = new SendCommandPanel();

        viewer = new SerialViewerColored();

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame.getContentPane().add(deviceSelector, BorderLayout.NORTH);
        mainFrame.getContentPane().add(viewer.getComponent(), BorderLayout.CENTER);
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
    public void handleEvent(AutoscrollSetting event) {
        viewer.setAutoScroll(event.get());
    }

    @Subscribe
    public void handleEvent(AddTimestampSetting event) {
        viewer.setAddTimestamps(event.get());
    }

    @Subscribe
    public void handleEvent(SerialMessageReceivedEvent event) {
        addText(Text.message(event.getValue(), event.getDate()));
    }

    @Subscribe
    public void handleEvent(DeviceCloseEvent event) {
        addText("Close serial: " + event.getConfig().getDevice());
    }

    @Subscribe
    public void handleEvent(DeviceOpenEvent event) {
        addText("Open serial: " + event.getConfig().getDevice());
    }

    @Subscribe
    public void handleEvent(DeviceErrorEvent event) {
        addText("Serial error: " + event.getMessage());
    }

    public void show() {
        mainFrame.setVisible(true);
    }
}
