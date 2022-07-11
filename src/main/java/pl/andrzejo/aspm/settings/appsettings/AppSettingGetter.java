/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings;

public class AppSettingGetter {

    public static <R, T extends AppSetting<R>> R get(Class<T> settingClass) {
        T object = AppSettingsFactory.create(settingClass);
        return object.get();
    }

}
