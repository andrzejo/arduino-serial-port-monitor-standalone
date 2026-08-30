/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer.model;

public enum MessageType {
    TIME,
    INTERNAL_INFO,
    INTERNAL_ERROR,
    SERIAL_DEBUG,
    SERIAL_INFO,
    SERIAL_WARN,
    SERIAL_ERROR;
}
