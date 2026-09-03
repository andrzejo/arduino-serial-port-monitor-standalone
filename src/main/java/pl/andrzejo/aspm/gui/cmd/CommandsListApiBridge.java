/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.cmd;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class CommandsListApiBridge {
    private final DefaultListModel<CommandItem> commandListModel = instance(DefaultListModel.class);

    public List<CommandItem> getCommands() {
        return Collections.list(commandListModel.elements());
    }

}
