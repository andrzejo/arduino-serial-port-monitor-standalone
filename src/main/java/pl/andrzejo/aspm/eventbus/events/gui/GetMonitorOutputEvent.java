/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2024.
 */

package pl.andrzejo.aspm.eventbus.events.gui;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class GetMonitorOutputEvent extends BusEvent {
    private final boolean withMessages;

    public GetMonitorOutputEvent(boolean withMessages) {
        this.withMessages = withMessages;
    }

    public boolean isWithMessages() {
        return withMessages;
    }
}
