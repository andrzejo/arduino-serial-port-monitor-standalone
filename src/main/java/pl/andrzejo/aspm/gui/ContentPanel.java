package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.settings.appsettings.AppSetting;
import pl.andrzejo.aspm.settings.types.BoolSetting;
import pl.andrzejo.aspm.settings.types.StringSetting;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ContentPanel extends JPanel {
    public static int PREFERRED_COMBO_WIDTH = 120;

    protected void handleComboSetting(JComboBox<String> box, StringSetting setting) {
        box.setSelectedItem(setting.get());
        box.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setting.set((String) box.getSelectedItem());
            }
        });
    }

    protected void handleCheckboxSetting(JCheckBox box, AppSetting<Boolean> setting) {
        box.setSelected(setting.get());
        box.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setting.set(box.isSelected());
            }
        });
    }

    protected JPanel createLabeled(String label, JComponent component) {
        JPanel panel = new JPanel();
        GridLayout layout = new GridLayout(2, 1);
        panel.setLayout(layout);
        panel.add(new JLabel(label));
        panel.add(component);
        return panel;
    }

    protected void setComboItems(JComboBox<String> combo, String[] items) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(items);
        combo.setModel(model);
    }

    protected void setComboBoxSize(JComboBox<String> box, Dimension dimension) {
        box.setMinimumSize(dimension);
        box.setPreferredSize(dimension);
    }

    protected void setPreferredComboBoxSize(JComboBox<String> box) {
        Dimension dimension = new Dimension(PREFERRED_COMBO_WIDTH, box.getPreferredSize().height);
        setComboBoxSize(box, dimension);
    }
}
