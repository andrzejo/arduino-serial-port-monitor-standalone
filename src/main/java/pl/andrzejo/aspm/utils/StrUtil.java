/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2024.
 */

package pl.andrzejo.aspm.utils;

public class StrUtil {
    public static boolean contains(String text, String contains) {
        if (text == null || contains == null) {
            return false;
        }
        return text.contains(contains);
    }
}
