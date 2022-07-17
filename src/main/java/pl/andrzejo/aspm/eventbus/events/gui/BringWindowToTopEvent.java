/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.events.gui;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class BringWindowToTopEvent extends BusEvent {
    private final boolean blur;

    public BringWindowToTopEvent(boolean blur) {
        this.blur = blur;
    }

    public boolean isBlur() {
        return blur;
    }
}
