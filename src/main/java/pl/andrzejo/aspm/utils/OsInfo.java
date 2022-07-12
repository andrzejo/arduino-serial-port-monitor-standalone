/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.utils;

public class OsInfo {
    public final static OsName CurrentOs;

    static {
        String osName = System.getProperty("os.name");
        if (osName.equals("Linux")) {
            CurrentOs = OsName.Linux;
        } else if (osName.startsWith("Win")) {
            CurrentOs = OsName.Windows;
        } else {
            CurrentOs = OsName.Other;
        }
    }

    public enum OsName {
        Linux, Windows, Other
    }
}
