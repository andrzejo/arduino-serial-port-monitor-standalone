/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui.setting.device;

import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.guihandlers.CheckBoxSettingsHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

public class RtsSettingHandler extends CheckBoxSettingsHandler {
    public RtsSettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defConfig) {
        super(setting, config, config::setRTS, config.isRTS(), defConfig.isRTS(), true);
    }
}
