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

public class StopBitsSettingHandler extends ListSettingHandler<DeviceConfig, Float> {

    public StopBitsSettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defConfig) {
        super(setting, config, config::setStopBits, config::getStopBits, defConfig.getStopBits(), true);
    }

    @Override
    protected void fillItems(LinkedHashMap<Float, String> items) {
        items.put(1f, "1");
        items.put(1.5f, "1.5");
        items.put(2f, "2");
    }
}
