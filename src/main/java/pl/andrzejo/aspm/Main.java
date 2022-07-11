package pl.andrzejo.aspm;

import com.formdev.flatlaf.FlatLightLaf;
import pl.andrzejo.aspm.api.AppApiService;
import pl.andrzejo.aspm.error.DefaultErrorHandler;
import pl.andrzejo.aspm.gui.SerialPortMonitorForm;
import pl.andrzejo.aspm.service.DeviceWatcherService;
import pl.andrzejo.aspm.service.SerialHandlerService;

import javax.swing.*;
import java.awt.*;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class Main {

    public static void main(String[] args) {

        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize theme. Using fallback.");
        }

        SerialPortMonitorForm form = new SerialPortMonitorForm();

        instance(SerialHandlerService.class).start();
        instance(DeviceWatcherService.class).start();
        instance(AppApiService.class).start();

        DefaultErrorHandler handler = new DefaultErrorHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);

        form.show();
    }

}
