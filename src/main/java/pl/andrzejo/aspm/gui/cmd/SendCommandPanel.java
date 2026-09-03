/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.cmd;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.api.cmd.ApiExecuteCommandEvent;
import pl.andrzejo.aspm.eventbus.events.command.ExecuteCommandEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceCloseEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceOpenEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.gui.ContentPanel;
import pl.andrzejo.aspm.gui.setting.LineEndingSettingHandler;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.monitor.LineEndingSetting;
import pl.andrzejo.aspm.utils.AppFiles;
import pl.andrzejo.aspm.utils.Serializer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.*;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.*;

public class SendCommandPanel extends ContentPanel {
    private static final Logger logger = LoggerFactory.getLogger(SendCommandPanel.class);
    private static final int MAX_HIST_ITEMS = 30;
    public static final Color DARK_GREEN = new Color(0, 100, 0);
    private final File histFile;
    private final File cmdFile;
    private final LineEndingSetting lineEndingSetting;
    private final DefaultListModel<CommandItem> commandListModel;
    private final JList<CommandItem> commandList;
    private final CommandItemRenderer cellRenderer = new CommandItemRenderer();
    private final JComboBox<String> commandEdit = new JComboBox<>();
    private final JTextField descriptionField = new JTextField();
    private final JButton sendBtn = new JButton("Send");
    private final JButton addButton = new JButton("+");
    private final JButton saveButton = new JButton("✔");
    private final JButton delButton = new JButton("\uD83D\uDDD1");
    private final JButton upButton = new JButton("⇧");
    private final JButton downButton = new JButton("⇩");
    private final Serializer serialization;
    private CommandItem editedItem;

    public SendCommandPanel() {
        serialization = instance(Serializer.class);
        histFile = new File(AppFiles.getAppConfigDir(), "history.txt");
        cmdFile = new File(AppFiles.getAppConfigDir(), "commands.json");
        commandListModel = instance(DefaultListModel.class);
        commandList = new JList<>(commandListModel);
        commandEdit.setEditable(true);
        JComboBox<String> lineEndingComboBox = new JComboBox<>();
        setPreferredWidthSize(lineEndingComboBox);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 10, 0, 10));
        JPanel lePanel = createLabeled("Line ending:", lineEndingComboBox);
        lePanel.setBorder(new EmptyBorder(-5, 0, 5, 0));
        sendBtn.addActionListener(handleAction(this::executeCommand));

        Font font = saveButton.getFont().deriveFont(Font.BOLD, 16f);

        addButton.setForeground(DARK_GREEN);
        upButton.setForeground(DARK_GREEN);
        downButton.setForeground(DARK_GREEN);
        saveButton.setForeground(Color.BLUE);
        delButton.setForeground(new Color(139, 0, 0));

        saveButton.setFont(font);
        addButton.setFont(font);
        downButton.setFont(font);
        upButton.setFont(font);
        delButton.setFont(font);

        addButton.setToolTipText("Add command");
        saveButton.setToolTipText("Save changes");
        delButton.setToolTipText("Delete selected");
        upButton.setToolTipText("Move command up");
        downButton.setToolTipText("Move command down");

        addButton.addActionListener(handleAction(this::addCommand));
        saveButton.addActionListener(handleAction(this::updateCommand));
        delButton.addActionListener(handleAction(this::deleteCommand));
        upButton.addActionListener(handleAction(this::moveCommandUp));
        downButton.addActionListener(handleAction(this::moveCommandDown));

        addButton.setEnabled(false);
        saveButton.setEnabled(false);
        delButton.setEnabled(false);

        JPanel editorPanel = createEditorPanel(commandEdit, sendBtn, addButton, saveButton, descriptionField);
        commandList.setCellRenderer(cellRenderer);
        commandList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        commandList.setFixedCellHeight(-1);

        JPanel movePanel = new JPanel(new BorderLayout());
        upButton.setEnabled(false);
        downButton.setEnabled(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.add(delButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.add(upButton);
        rightPanel.add(downButton);

        movePanel.add(leftPanel, BorderLayout.WEST);
        movePanel.add(rightPanel, BorderLayout.EAST);

        JScrollPane commandListScrollPane = new JScrollPane(commandList);
        commandListScrollPane.setPreferredSize(new Dimension(280, 300));

        JScrollPane scroll = new JScrollPane(commandList);
        JPanel commandsPanel = new JPanel(new BorderLayout(5, 5));

        commandsPanel.add(editorPanel, BorderLayout.NORTH);
        commandsPanel.add(scroll, BorderLayout.CENTER);
        commandsPanel.add(movePanel, BorderLayout.SOUTH);
        commandsPanel.setPreferredSize(new Dimension(280, 300));

        JLabel titleLabel = new JLabel("Send command");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

        add(titleLabel, BorderLayout.NORTH);
        add(commandsPanel, BorderLayout.CENTER);
        add(lePanel, BorderLayout.SOUTH);

        lineEndingSetting = AppSettingsFactory.create(LineEndingSetting.class);
        LineEndingSettingHandler handler = new LineEndingSettingHandler(lineEndingSetting);
        handler.setupComponent(lineEndingComboBox);
        toggleEnabled(false);
        instance(ApplicationEventBus.class).register(this);
        loadHistory();
        loadCommands();

        descriptionField
                .getDocument()
                .addDocumentListener(handleDocumentChange(e -> updateButtonsState()));

        commandEdit
                .getActionMap()
                .put("enterPressed", handleAction(e -> sendBtn.doClick()));

        JTextComponent editor = (JTextComponent) commandEdit.getEditor().getEditorComponent();
        editor
                .getDocument()
                .addDocumentListener(handleDocumentChange(e -> updateButtonsState()));

        commandList.addMouseListener(mouseClicked(e -> {
            if (e.getClickCount() == 2) {
                if (isOnActivePlayIcon(e)) {
                    CommandItem cmd = commandList.getSelectedValue();
                    executeCommand(cmd);
                } else {
                    editedItem = commandList.getSelectedValue();
                    cellRenderer.setEditIndex(commandList.getSelectedIndex());
                    commandEdit.getEditor().setItem(editedItem.getCommand());
                    descriptionField.setText(editedItem.getDescription());
                    commandEdit.requestFocusInWindow();
                    updateButtonsState();
                }
            }
        }));

        commandList
                .addListSelectionListener(e -> {
                    editedItem = null;
                    cellRenderer.setEditIndex(-1);
                    updateButtonsState();
                });

        commandList
                .addMouseMotionListener(mouseMoved(e -> {
                    if (isOnActivePlayIcon(e)) {
                        commandList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        commandList.setCursor(Cursor.getDefaultCursor());
                    }
                }));

        commandList
                .addMouseListener(mouseExited(e -> commandList.setCursor(Cursor.getDefaultCursor())));

        commandList
                .getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "executeCommand");

        commandList
                .getActionMap().put("executeCommand", handleAction(e -> {
                    if (sendBtn.isEnabled()) {
                        CommandItem cmd = commandList.getSelectedValue();
                        executeCommand(cmd);
                    }
                }));
    }

    private void moveCommandDown(ActionEvent actionEvent) {
        moveCommand(1);
    }

    private void moveCommandUp(ActionEvent actionEvent) {
        moveCommand(-1);
    }

    private void moveCommand(int dir) {
        int currentIndex = commandList.getSelectedIndex();
        if (currentIndex < 0) {
            return;
        }
        int targetIndex = currentIndex + dir;
        int size = commandListModel.getSize();

        if (targetIndex < 0 || targetIndex >= size) {
            return;
        }

        CommandItem item = commandListModel.getElementAt(currentIndex);
        commandListModel.removeElementAt(currentIndex);
        commandListModel.insertElementAt(item, targetIndex);
        commandList.setSelectedIndex(targetIndex);
        commandList.ensureIndexIsVisible(targetIndex);
        repaintList();
        updateButtonsState();
        saveCommands();
    }

    private boolean isOnActivePlayIcon(MouseEvent e) {
        int index = commandList.locationToIndex(e.getPoint());

        if (index < 0 || !sendBtn.isEnabled()) {
            return false;
        }

        Rectangle cell = commandList.getCellBounds(index, index);
        Rectangle playBounds = new Rectangle(cell.x + 2, cell.y + 12, 22, 22);
        return playBounds.contains(e.getPoint());
    }

    private void updateButtonsState() {
        CommandItem command = getCurrentCommand();
        boolean notBlank = isNotBlank(command.getCommand());
        addButton.setEnabled(notBlank || isNotBlank(command.getDescription()));
        saveButton.setEnabled(notBlank && editedItem != null);
        int index = commandList.getSelectedIndex();
        if (index >= 0) {
            upButton.setEnabled(index > 0);
            downButton.setEnabled(index < commandListModel.size() - 1);
            delButton.setEnabled(true);
        } else {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            delButton.setEnabled(false);
        }
    }

    private void updateCommand(ActionEvent actionEvent) {
        if (editedItem == null) {
            return;
        }

        int index = commandListModel.indexOf(editedItem);

        if (index >= 0) {
            CommandItem currentCommand = getCurrentCommand();
            commandListModel.set(index, currentCommand);
            editedItem = currentCommand;
            commandList.setSelectedIndex(index);
            commandList.ensureIndexIsVisible(index);
            saveCommands();
        }
        editedItem = null;
        cellRenderer.setEditIndex(-1);

        repaintList();
    }

    private void deleteCommand(ActionEvent actionEvent) {
        int index = commandList.getSelectedIndex();
        if (index >= 0) {
            commandListModel.remove(index);
            editedItem = null;
            saveCommands();
        }
        repaintList();
    }

    private void repaintList() {
        commandList.revalidate();
        commandList.repaint();
    }

    private void addCommand(ActionEvent actionEvent) {
        CommandItem cmd = getCurrentCommand();
        if (isNotBlank(cmd.getCommand()) || isNotBlank(cmd.getDescription())) {
            int idx = commandList.getSelectedIndex();
            commandListModel.add(idx + 1, cmd);
            saveCommands();
        }
        repaintList();
    }

    private void executeCommand(ActionEvent actionEvent) {
        executeCommand(getCurrentCommand());
    }

    private void executeCommand(CommandItem cmd) {
        if (!cmd.getCommand().isEmpty()) {
            String lineEnding = getLineEnding();
            instance(ApplicationEventBus.class).post(new ExecuteCommandEvent(cmd, lineEnding));
            addToHistory(cmd.getCommand());
        }
    }

    private static JPanel createEditorPanel(JComboBox<String> commandField, JButton sendBtn,
                                            JButton addBtn, JButton saveBtn, JTextField commentField) {
        JPanel editorPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        editorPanel.add(new JLabel("Cmd:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        editorPanel.add(commandField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        editorPanel.add(new JLabel("Desc:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        editorPanel.add(commentField, gbc);

        gbc.gridy = 2;

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        editorPanel.add(sendBtn, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        editorPanel.add(addBtn, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0;
        editorPanel.add(saveBtn, gbc);

        return editorPanel;
    }

    private CommandItem getCurrentCommand() {
        JTextComponent editor = (JTextComponent) commandEdit.getEditor().getEditorComponent();
        String cmd = editor.getText();
        return new CommandItem(cmd, descriptionField.getText());
    }

    private void addToHistory(String command) {
        if (isBlank(command)) {
            return;
        }
        List<String> items = getHistoryItems();
        if (items.contains(command.trim())) {
            return;
        }

        commandEdit.insertItemAt(command, 0);

        if (commandEdit.getItemCount() > MAX_HIST_ITEMS) {
            commandEdit.removeItemAt(MAX_HIST_ITEMS);
        }

        saveHistory();
    }

    private String getLineEnding() {
        return lineEndingSetting.get();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceCloseEvent event) {
        toggleEnabled(false);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceOpenEvent event) {
        toggleEnabled(true);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ApiExecuteCommandEvent event) {
        String lineEnding = getLineEnding();
        CommandItem cmd = new CommandItem(event.getCommand(), "API command");
        instance(ApplicationEventBus.class).post(new ExecuteCommandEvent(cmd, lineEnding));
    }

    private void toggleEnabled(boolean enabled) {
        sendBtn.setEnabled(enabled);
        cellRenderer.setSendEnabled(enabled);
        commandList.repaint();
    }

    private void loadHistory() {
        try {
            if (histFile.isFile()) {
                List<String> items = FileUtils.readLines(histFile, StandardCharsets.UTF_8);
                items.forEach(commandEdit::addItem);
            }
        } catch (Exception e) {
            logger.warn("Failed to read history from file " + histFile, e);
        }
    }

    private void loadCommands() {
        try {
            if (cmdFile.isFile()) {
                String json = FileUtils.readFileToString(cmdFile, StandardCharsets.UTF_8);
                Type type = new TypeToken<List<CommandItem>>() {
                }.getType();
                ArrayList<CommandItem> list = serialization.deserialize(json, type);
                list.forEach(commandListModel::addElement);
            }
        } catch (Exception e) {
            logger.warn("Failed to read commands from file " + cmdFile, e);
        }
    }

    private void saveCommands() {
        try {
            ArrayList<CommandItem> list = Collections.list(commandListModel.elements());
            String json = serialization.serialize(list);
            FileUtils.writeStringToFile(cmdFile, json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("Failed to save commands to file " + cmdFile, e);
        }
    }

    private void saveHistory() {
        try {
            FileUtils.writeLines(histFile, getHistoryItems());
        } catch (Exception e) {
            logger.warn("Failed to save history to file " + histFile, e);
        }
    }

    private List<String> getHistoryItems() {
        ArrayList<String> hists = new ArrayList<>();
        for (int i = 0; i < commandEdit.getItemCount(); i++) {
            String item = trimToEmpty(commandEdit.getItemAt(i));
            if (isNotBlank(item)) {
                hists.add(item);
            }
        }
        return hists;
    }

}
