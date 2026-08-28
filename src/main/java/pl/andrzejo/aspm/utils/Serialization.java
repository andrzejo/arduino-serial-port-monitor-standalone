/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class Serialization {
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public String serialize(Object object) {
        return gson.toJson(object);
    }

    public <T> T deserialize(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public <T> T deserialize(String json, Type type) {
        return gson.fromJson(json, type);
    }
}
