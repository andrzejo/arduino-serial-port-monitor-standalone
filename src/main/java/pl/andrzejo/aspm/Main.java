package pl.andrzejo.aspm;

import com.formdev.flatlaf.FlatLightLaf;
import pl.andrzejo.aspm.gui.SerialPortMonitorForm;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize theme. Using fallback.");
        }

        EventQueue.invokeLater(() -> {
            SerialPortMonitorForm form = new SerialPortMonitorForm();
            form.show();
        });

    }

}
