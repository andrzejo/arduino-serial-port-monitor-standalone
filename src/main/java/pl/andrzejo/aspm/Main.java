package pl.andrzejo.aspm;

import com.formdev.flatlaf.FlatLightLaf;
import pl.andrzejo.aspm.api.RestApiService;
import pl.andrzejo.aspm.error.DefaultErrorHandler;
import pl.andrzejo.aspm.gui.SerialPortMonitorForm;
import pl.andrzejo.aspm.service.DeviceWatcherService;
import pl.andrzejo.aspm.service.SerialHandlerService;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        new SerialHandlerService().start();
        new DeviceWatcherService().start();
        new RestApiService().start();

        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize theme. Using fallback.");
        }

        EventQueue.invokeLater(() -> {
            DefaultErrorHandler handler = new DefaultErrorHandler();
            Thread.setDefaultUncaughtExceptionHandler(handler);

            SerialPortMonitorForm form = new SerialPortMonitorForm();
            form.show();
        });
    }

}
