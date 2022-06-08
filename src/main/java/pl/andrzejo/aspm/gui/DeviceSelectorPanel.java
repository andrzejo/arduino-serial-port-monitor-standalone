package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.serial.SerialPortList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DeviceSelectorPanel extends JPanel {
    private final JCheckBox autoScroll;
    private final JCheckBox addTimestamp;
    private final JCheckBox autoOpenBox;
    protected String[] serialRateStrings = {"300", "1200", "2400", "4800", "9600", "19200", "38400", "57600", "74880", "115200", "230400", "250000", "500000", "1000000", "2000000"};

    JComboBox<String> deviceComboBox = new JComboBox<>();
    JComboBox<String> baudComboBox = new JComboBox<>();

    public DeviceSelectorPanel() {
        loadItems();
//        setBackground(Color.pink);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(1, 5, 15, 5));

        JPanel devPanel = createLabeled("Device:", deviceComboBox);
        JPanel baudPanel = createLabeled("Baud:", baudComboBox);

        JButton openBtn = new JButton("Open");
        autoOpenBox = new JCheckBox("Auto open");

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(2, 1, 15, 0));
        btnPanel.add(autoOpenBox);
        btnPanel.add(openBtn);


        JPanel dev = new JPanel();
        dev.setLayout(new GridLayout(1, 3, 15, 0));
        dev.add(devPanel);
        dev.add(baudPanel);
        dev.add(btnPanel);

        add(dev, BorderLayout.WEST);
        JPanel boxes = new JPanel();
        boxes.setLayout(new GridLayout(2, 1));
        autoScroll = new JCheckBox("Auto scroll");
        addTimestamp = new JCheckBox("Add timestamp");

        boxes.add(autoScroll);
        boxes.add(addTimestamp);
        add(boxes, BorderLayout.EAST);

        Dimension dimension = new Dimension(120, baudComboBox.getMinimumSize().height);
        baudComboBox.setMinimumSize(dimension);
        deviceComboBox.setMinimumSize(dimension);

        baudComboBox.setPreferredSize(dimension);
        deviceComboBox.setPreferredSize(dimension);
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
        setComboItems(deviceComboBox, portNames, "");
        setComboItems(baudComboBox, serialRateStrings, "9600");
    }

    private void setComboItems(JComboBox<String> combo, String[] items, String selected) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(items);
        combo.setModel(model);
        combo.setSelectedItem(selected);
    }
}
