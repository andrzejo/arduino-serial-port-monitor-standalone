/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings.items.monitor;

import pl.andrzejo.aspm.settings.Setting;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;

public abstract class GroupMonitorSetting<T> extends AppSetting<T> {
    protected GroupMonitorSetting(Class<? extends Setting<T>> setting, String name, T defValue) {
        super(setting, "monitor", name, defValue);
    }
}
