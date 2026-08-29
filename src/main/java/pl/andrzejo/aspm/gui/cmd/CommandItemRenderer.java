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

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

final class CommandItemRenderer extends JPanel implements ListCellRenderer<CommandItem> {
    private final JPanel commandPanel = new JPanel(new BorderLayout(8, 2));
    private final JLabel executeLabel = new JLabel("▶");
    private final JLabel commandLabel = new JLabel();
    private final JLabel descriptionLabel = new JLabel();

    private final JPanel groupPanel = new JPanel(new BorderLayout());
    private final JLabel groupIconLabel = new JLabel("☰");
    private final JLabel groupLabel = new JLabel();
    private int editIndex = -1;

    private static final Color EDIT_COLOR = Color.BLUE;
    private static final Color EXEC_COLOR = new Color(139, 0, 0);
    private boolean isEnabled;

    public CommandItemRenderer() {
        createCommandPanel();
        createGroupPanel();
    }

    private void createCommandPanel() {
        commandPanel.setBorder(new EmptyBorder(2, 4, 2, 6));
        commandPanel.setOpaque(true);

        executeLabel.setFont(executeLabel.getFont().deriveFont(Font.BOLD, 22f));
        commandLabel.setFont(commandLabel.getFont().deriveFont(Font.BOLD));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(commandLabel);
        textPanel.add(descriptionLabel);

        commandPanel.add(executeLabel, BorderLayout.WEST);
        commandPanel.add(textPanel, BorderLayout.CENTER);
    }

    private void createGroupPanel() {
        groupPanel.setBorder(new EmptyBorder(2, 4, 2, 6));
        groupPanel.setOpaque(true);

        groupLabel.setHorizontalAlignment(SwingConstants.CENTER);
        groupLabel.setFont(groupLabel.getFont().deriveFont(Font.BOLD));
        //groupPanel.add(groupIconLabel, BorderLayout.WEST);
        groupPanel.add(groupLabel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends CommandItem> list, CommandItem value,
                                                  int index, boolean isSelected, boolean cellHasFocus
    ) {
        boolean isEdit = editIndex >= 0 && editIndex == index;
        boolean isGroup = isBlank(value.getCommand()) && isNotBlank(value.getDescription());
        if (isGroup) {
            return renderGroup(list, value, isSelected, isEdit);
        }
        return renderCommand(list, value, isSelected, isEdit);
    }

    private Component renderGroup(JList<?> list, CommandItem item, boolean selected, boolean edit) {
        groupLabel.setText(item.getDescription());
        groupPanel.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
        Color color = edit ? EDIT_COLOR : Color.GRAY;
        groupLabel.setForeground(color);
        groupIconLabel.setForeground(color);
        return groupPanel;
    }

    private Component renderCommand(JList<?> list, CommandItem value, boolean selected, boolean edit) {
        commandLabel.setText((edit ? "Editing " : "") + value.getCommand());
        descriptionLabel.setText(value.getDescription());

        commandLabel.setEnabled(isEnabled || edit);
        executeLabel.setEnabled(isEnabled || edit);
        descriptionLabel.setEnabled(isEnabled || edit);

        executeLabel.setText(edit ? "✎" : "▶");

        if (selected) {
            commandPanel.setBackground(list.getSelectionBackground());
            Color color = edit ? EDIT_COLOR : list.getSelectionForeground();
            commandLabel.setForeground(color);
            descriptionLabel.setForeground(edit ? Color.GRAY : list.getSelectionForeground());
            executeLabel.setForeground(edit ? EDIT_COLOR : EXEC_COLOR);
        } else {
            commandPanel.setBackground(list.getBackground());
            Color color = edit ? EDIT_COLOR : list.getForeground();
            commandLabel.setForeground(color);
            executeLabel.setForeground(edit ? EDIT_COLOR : EXEC_COLOR);
            descriptionLabel.setForeground(Color.GRAY);
        }

        return commandPanel;
    }

    public void setSendEnabled(boolean enabled) {
        isEnabled = enabled;
        executeLabel.setEnabled(enabled);
        commandLabel.setEnabled(enabled);
        descriptionLabel.setEnabled(enabled);
    }

    public void setEditIndex(int selectedIndex) {
        this.editIndex = selectedIndex;
    }
}
