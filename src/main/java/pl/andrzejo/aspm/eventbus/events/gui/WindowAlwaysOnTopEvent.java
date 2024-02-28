/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2024.
 */

package pl.andrzejo.aspm.eventbus.events.gui;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class WindowAlwaysOnTopEvent extends BusEvent {
    private final boolean alwaysOnTop;

    public WindowAlwaysOnTopEvent(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
    }

    public boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }
}
