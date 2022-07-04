package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.App;

import javax.swing.*;
import java.awt.*;

public class AboutForm extends JDialog {

    public AboutForm(JFrame parent) throws HeadlessException {
        super(parent, true);
        setTitle("About " + App.Name);
    }

    public void showModal() {
        setSize(400, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
