/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.cmd;

import pl.andrzejo.aspm.api.server.NotFoundException;

import java.util.List;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class CommandsListApiBridge {
    private final SendCommandPanel sendCommandPanel = instance(SendCommandPanel.class);

    public List<CommandItem> getCommands() {
        return sendCommandPanel.getCommands();
    }

    public void update(int index, CommandItem cmd) {
        validateIndex(index);
        sendCommandPanel.updateCmd(index, cmd);
    }

    public void delete(int index) {
        validateIndex(index);
        sendCommandPanel.removeCmd(index);
    }

    public int add(CommandItem cmd) {
        return sendCommandPanel.addCmd(cmd);
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= getCommands().size()) {
            throw new NotFoundException();
        }
    }
}
