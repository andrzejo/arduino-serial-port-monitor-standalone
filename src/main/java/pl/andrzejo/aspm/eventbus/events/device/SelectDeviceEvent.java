/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.events.device;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class SelectDeviceEvent extends BusEvent {
    private final String device;

    public SelectDeviceEvent(String device) {
        this.device = device;
    }

    public String getDevice() {
        return device;
    }
}
