/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.events.device;

import pl.andrzejo.aspm.eventbus.events.BusEvent;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

public class DeviceCloseEvent extends BusEvent {
    private final DeviceConfig config;

    public DeviceCloseEvent(DeviceConfig config) {
        super();
        this.config = config;
    }

    public DeviceConfig getConfig() {
        return config;
    }
}
