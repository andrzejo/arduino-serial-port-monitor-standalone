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

public class BaudSettingHandler extends ListSettingHandler<DeviceConfig, Integer> {
    protected static Integer[] serialRates = {300, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 74880, 115200, 230400, 250000, 500000, 1000000, 2000000};

    public BaudSettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defValue) {
        super(setting, config, config::setBaud, config::getBaud, defValue.getBaud(), false);
    }

    @Override
    protected void fillItems(LinkedHashMap<Integer, String> items) {
        for (Integer rate : serialRates) {
            items.put(rate, getLabel(rate));
        }
    }

    private String getLabel(Integer rate) {
        if (rate.equals(defValue)) {
            return rate + " (default)";
        }
        return rate.toString();
    }
}
