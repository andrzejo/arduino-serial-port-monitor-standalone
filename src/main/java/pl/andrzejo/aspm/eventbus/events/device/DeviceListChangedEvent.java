/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.events.device;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

import java.util.List;

public class DeviceListChangedEvent extends BusEvent {
    private final List<String> devices;

    public DeviceListChangedEvent(List<String> devices) {
        this.devices = devices;
    }

    public List<String> getDevices() {
        return devices;
    }
}
