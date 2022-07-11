/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings.items.monitor;

import pl.andrzejo.aspm.settings.types.BoolSetting;

public class AutoOpenSetting extends GroupMonitorSetting<Boolean> {
    protected AutoOpenSetting() {
        super(BoolSetting.class, "autoOpenDevice", false);
    }
}
