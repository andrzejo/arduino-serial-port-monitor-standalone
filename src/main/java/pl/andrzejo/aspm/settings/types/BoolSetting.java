/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.types;

import pl.andrzejo.aspm.settings.Setting;

public class BoolSetting extends Setting<Boolean> {

    public BoolSetting(String key, Boolean defValue) {
        super(key, defValue);
    }

    @Override
    protected String serialize(Boolean value) {
        return String.valueOf(value);
    }

    @Override
    protected Boolean deserialize(String value) {
        return Boolean.parseBoolean(value);
    }
}
