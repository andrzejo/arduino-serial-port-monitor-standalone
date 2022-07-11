/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.events.api.commands;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class ApiOpenDeviceEvent extends BusEvent {
    private final String device;

    public ApiOpenDeviceEvent(String device) {
        this.device = device;
    }

    public String getDevice() {
        return device;
    }
}
