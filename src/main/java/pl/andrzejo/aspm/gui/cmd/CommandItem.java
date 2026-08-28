/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.cmd;

public class CommandItem {
    private final String command;
    private final String description;

    CommandItem(String command, String comment) {
        this.command = command;
        this.description = comment;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }
}
