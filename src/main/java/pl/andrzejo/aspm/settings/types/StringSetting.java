/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.types;

import pl.andrzejo.aspm.settings.Setting;

public class StringSetting extends Setting<String> {
    public StringSetting(String key) {
        super(key);
    }

    public StringSetting(String key, String defValue) {
        super(key, defValue);
    }

    @Override
    protected String serialize(String value) {
        return value;
    }

    @Override
    protected String deserialize(String value) {
        return value;
    }
}
