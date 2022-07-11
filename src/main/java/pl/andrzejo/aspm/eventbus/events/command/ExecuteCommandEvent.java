/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.events.command;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class ExecuteCommandEvent extends BusEvent {
    private final String command;
    private final String lineEnding;

    public ExecuteCommandEvent(String command, String lineEnding) {
        this.command = command;
        this.lineEnding = lineEnding;
    }

    public String getCommand() {
        return command;
    }

    public String getLineEnding() {
        return lineEnding;
    }
}
