/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.command.ExecuteCommandEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceCloseEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceOpenEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.gui.setting.LineEndingSettingHandler;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.monitor.LineEndingSetting;
import pl.andrzejo.aspm.utils.AppFiles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.*;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;

public class SendCommandPanel extends ContentPanel {
    private static final Logger logger = LoggerFactory.getLogger(SendCommandPanel.class);

    private static final int MAX_HIST_ITEMS = 15;
    private final File histFile;
    JComboBox<String> lineEndingComboBox = new JComboBox<>();
    JComboBox<String> commandEdit = new JComboBox<>();
    JButton sendBtn = new JButton("Send");

    public SendCommandPanel() {
        histFile = new File(AppFiles.getAppConfigDir(), "history.txt");
        commandEdit.setEditable(true);
        setPreferredWidthSize(lineEndingComboBox);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(1, 5, 10, 5));
        JPanel lePanel = createLabeled("Line ending:", lineEndingComboBox);
        JPanel commandPanel = createLabeled("Command:", commandEdit);
        JPanel btnPanel = createLabeled("", sendBtn);
        sendBtn.addActionListener(handleAction(this::executeCommand));

        add(lePanel, BorderLayout.WEST);
        add(commandPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.EAST);
        LineEndingSettingHandler handler = new LineEndingSettingHandler(AppSettingsFactory.create(LineEndingSetting.class));
        handler.setupComponent(lineEndingComboBox);
        toggleEnabled(false);
        instance(ApplicationEventBus.class).register(this);
        loadHistory();
    }

    private void executeCommand(ActionEvent actionEvent) {
        Object current = commandEdit.getSelectedItem();
        String command = current == null ? "" : current.toString();
        addToHistory(command);
        String lineEnding = getLineEnding();
        instance(ApplicationEventBus.class).post(new ExecuteCommandEvent(command, lineEnding));
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
        return "\n";
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

    private void toggleEnabled(boolean enabled) {
        sendBtn.setEnabled(enabled);
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
