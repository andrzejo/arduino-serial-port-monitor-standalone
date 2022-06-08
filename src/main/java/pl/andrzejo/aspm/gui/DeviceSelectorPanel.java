package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.serial.SerialPortList;

import javax.swing.*;
import java.awt.*;

public class DeviceSelectorPanel extends JPanel {
    JComboBox<String> deviceComboBox = new JComboBox<>();
    JComboBox<String> baudComboBox = new JComboBox<>();

    public DeviceSelectorPanel(SerialPortMonitorForm parent) {
        loadItems();
        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 1));
        JPanel devPanel = createLabeled("Device:", deviceComboBox);
        JPanel baudPanel = createLabeled("Baud:", new JComboBox<>());
        add(devPanel);
        add(baudPanel);
        JPanel boxes = new JPanel();
        boxes.setLayout(new GridLayout(2, 1));
        boxes.add(new JCheckBox("Auto scroll"));
        boxes.add(new JCheckBox("Add timestamp"));
        add(boxes);
    }

    private JPanel createLabeled(String label, JComponent component) {
        JPanel panel = new JPanel();
        GridLayout layout = new GridLayout(2, 1);
        panel.setLayout(layout);
        panel.add(new JLabel(label));
        panel.add(component);
        return panel;
    }

    private void loadItems() {
        String[] portNames = SerialPortList.getPortNames();
        for (String portName : portNames) {
            deviceComboBox.addItem(portName);
        }
    }
}
