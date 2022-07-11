/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings;

import pl.andrzejo.aspm.settings.repository.SettingsRepository;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public abstract class Setting<T> {
    protected final SettingsRepository repository;
    protected final String key;
    protected final T defValue;

    protected Setting(String key) {
        this(key, null);
    }

    protected Setting(String key, T defValue) {
        this.key = key;
        this.defValue = defValue;
        this.repository = instance(SettingsRepository.class);
    }

    public T get() {
        String val = repository.getString(key, null);
        if (val == null) {
            return defValue;
        }

        try {
            return deserialize(val);
        } catch (Exception e) {
            return defValue;
        }
    }

    public void set(T value) {
        String val = serialize(value);
        repository.saveString(key, val);
    }

    protected abstract String serialize(T value);

    protected abstract T deserialize(String value);
}
