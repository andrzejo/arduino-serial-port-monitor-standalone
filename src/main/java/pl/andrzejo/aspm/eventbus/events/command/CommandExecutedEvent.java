/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.events.command;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class CommandExecutedEvent extends BusEvent {
    private final String command;

    public CommandExecutedEvent(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
