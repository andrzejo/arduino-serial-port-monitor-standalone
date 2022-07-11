/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.events.api.commands;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class ApiExecuteCommand extends BusEvent {
    private final String command;
    private final String body;

    public ApiExecuteCommand(String command, String body) {
        this.command = command;
        this.body = body;
    }

    public String getCommand() {
        return command;
    }

    public String getBody() {
        return body;
    }
}
