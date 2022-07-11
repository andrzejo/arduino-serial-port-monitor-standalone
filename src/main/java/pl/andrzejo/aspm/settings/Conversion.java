/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings;

import java.util.function.Function;

public class Conversion {
    public static int toInt(String val, int def) {
        return parse(def, val, Integer::parseInt);
    }

    public static float toFloat(String val, float def) {
        return parse(def, val, Float::parseFloat);
    }

    public static boolean toBool(String val, boolean def) {
        return parse(def, val, Boolean::parseBoolean);
    }

    private static <T> T parse(T def, String val, Function<String, T> converter) {
        try {
            return converter.apply(val);
        } catch (Exception e) {
            return def;
        }
    }
}
