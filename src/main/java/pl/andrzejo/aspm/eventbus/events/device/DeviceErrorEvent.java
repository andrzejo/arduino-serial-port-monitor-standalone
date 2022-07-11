/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.events.device;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class DeviceErrorEvent extends BusEvent {
    private String message;

    public DeviceErrorEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
