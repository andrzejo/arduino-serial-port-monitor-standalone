/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings.items.device;

import pl.andrzejo.aspm.settings.types.DeviceConfig;
import pl.andrzejo.aspm.settings.types.DeviceSetting;

public class TtyDeviceSetting extends GroupDeviceSetting<DeviceConfig> {

    protected TtyDeviceSetting() {
        super(DeviceSetting.class, "tty", DeviceConfig.defaultConfig());
    }
}
