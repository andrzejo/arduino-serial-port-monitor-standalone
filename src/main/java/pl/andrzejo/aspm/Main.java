package pl.andrzejo.aspm;

import com.formdev.flatlaf.FlatLightLaf;
import pl.andrzejo.aspm.gui.SerialPortMonitorForm;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize theme. Using fallback.");
        }

        SerialPortMonitorForm form = new SerialPortMonitorForm();
        form.show();
    }

}
