/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings.items.device;

import pl.andrzejo.aspm.settings.Setting;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;

public abstract class GroupDeviceSetting<T> extends AppSetting<T> {
    protected GroupDeviceSetting(Class<? extends Setting<T>> setting, String name, T defValue) {
        super(setting, "device", name, defValue);
    }
}
