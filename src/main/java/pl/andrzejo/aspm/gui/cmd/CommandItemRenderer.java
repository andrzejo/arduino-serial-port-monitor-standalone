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
    private int editIndex = -1;

    private static final Color EDIT_COLOR = Color.BLUE;
    private static final Color EXEC_COLOR = new Color(139, 0, 0);
    private boolean isEnabled;

    public CommandItemRenderer() {
        setLayout(new BorderLayout(8, 2));
        setBorder(new EmptyBorder(2, 4, 2, 6));

        executeLabel.setFont(executeLabel.getFont().deriveFont(Font.BOLD, 22f));

        commandLabel.setFont(commandLabel.getFont().deriveFont(Font.BOLD));

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
        isEnabled = enabled;
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
        boolean edit = editIndex >= 0 && editIndex == index;

        commandLabel.setText((edit ? "Editing " : "") + value.getCommand());
        descriptionLabel.setText(value.getDescription());
        executeLabel.setText(edit ? "✎" : "▶");

        commandLabel.setEnabled(isEnabled || edit);
        executeLabel.setEnabled(isEnabled || edit);
        descriptionLabel.setEnabled(isEnabled || edit);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            Color color = edit ? EDIT_COLOR : list.getSelectionForeground();
            commandLabel.setForeground(color);
            descriptionLabel.setForeground(edit ? Color.GRAY : list.getSelectionForeground());
            executeLabel.setForeground(edit ? EDIT_COLOR : EXEC_COLOR);
        } else {
            setBackground(list.getBackground());
            Color color = edit ? EDIT_COLOR : list.getForeground();
            commandLabel.setForeground(color);
            executeLabel.setForeground(edit ? EDIT_COLOR : EXEC_COLOR);
            descriptionLabel.setForeground(Color.GRAY);
        }

        return this;
    }

    public void setEditIndex(int selectedIndex) {
        this.editIndex = selectedIndex;
    }
}
