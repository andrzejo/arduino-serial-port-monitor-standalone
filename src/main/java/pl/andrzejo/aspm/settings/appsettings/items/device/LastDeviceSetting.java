/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings.items.device;

import pl.andrzejo.aspm.settings.types.StringSetting;

public class LastDeviceSetting extends GroupDeviceSetting<String> {
    protected LastDeviceSetting() {
        super(StringSetting.class, "lastDevice", null);
    }
}
