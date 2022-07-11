/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui.setting.device;

import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.guihandlers.ListSettingHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.util.LinkedHashMap;


public class ParitySettingHandler extends ListSettingHandler<DeviceConfig, Character> {
    public ParitySettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defValue) {
        super(setting, config, config::setParity, config::getParity, defValue.getParity(), true);
    }

    @Override
    protected void fillItems(LinkedHashMap<Character, String> items) {
        items.put('N', "none");
        items.put('E', "even");
        items.put('O', "odd");
    }
}
