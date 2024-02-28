/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.BusEvent;
import pl.andrzejo.aspm.eventbus.impl.EventBus;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;
import pl.andrzejo.aspm.settings.types.StringSetting;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class ContentPanel extends JPanel {
    public static int PREFERRED_COMBO_WIDTH = 120;
    protected final ApplicationEventBus eventBus;

    public ContentPanel() {
        eventBus = instance(ApplicationEventBus.class);
    }

    protected void handleComboSetting(JComboBox<String> box, AppSetting<String> setting) {
        box.setSelectedItem(setting.get());
        box.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setting.set((String) box.getSelectedItem());
            }
        });
    }

    protected void handleCheckboxSetting(JCheckBox box, AppSetting<Boolean> setting, Consumer<Boolean> handler) {
        box.setSelected(setting.get());
        box.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = box.isSelected();
                setting.set(selected);
                if (handler != null) {
                    handler.accept(selected);
                }
            }
        });
    }

    protected void handleCheckboxSetting(JCheckBox box, AppSetting<Boolean> setting) {
        handleCheckboxSetting(box, setting, null);
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

    protected void setComboBoxSize(JComponent box, Dimension dimension) {
        box.setMinimumSize(dimension);
        box.setPreferredSize(dimension);
    }

    protected void setPreferredWidthSize(JComponent box) {
        Dimension dimension = new Dimension(PREFERRED_COMBO_WIDTH, box.getPreferredSize().height);
        setComboBoxSize(box, dimension);
    }
}
