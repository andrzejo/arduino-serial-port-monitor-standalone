/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings;

import pl.andrzejo.aspm.factory.BeanFactory;

public class AppSettingsFactory {

    public static <T extends AppSetting<?>> T create(Class<T> appSettingType) {
        return BeanFactory.instance(appSettingType);
    }

}
