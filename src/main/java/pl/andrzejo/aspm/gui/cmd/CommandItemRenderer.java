/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.cmd;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class CommandItemRenderer extends JPanel implements ListCellRenderer<CommandItem> {
    private final JLabel executeLabel = new JLabel("▶");
    private final JLabel commandLabel = new JLabel();
    private final JLabel descriptionLabel = new JLabel();

    public CommandItemRenderer() {
        setLayout(new BorderLayout(8, 2));
        setBorder(new EmptyBorder(5, 6, 5, 6));

        commandLabel.setFont(commandLabel.getFont().deriveFont(Font.BOLD));
        descriptionLabel.setForeground(Color.GRAY);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.add(commandLabel);
        textPanel.add(descriptionLabel);

        add(executeLabel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);

        setOpaque(true);
    }

    public void setSendEnabled(boolean enabled) {
        executeLabel.setEnabled(enabled);
        commandLabel.setEnabled(enabled);
        descriptionLabel.setEnabled(enabled);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends CommandItem> list,
                                                  CommandItem value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus
    ) {
        commandLabel.setText(value.getCommand());
        descriptionLabel.setText(value.getDescription());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            commandLabel.setForeground(list.getSelectionForeground());
            executeLabel.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            commandLabel.setForeground(list.getForeground());
            executeLabel.setForeground(list.getForeground());
        }

        return this;
    }
}
