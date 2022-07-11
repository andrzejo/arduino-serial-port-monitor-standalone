/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui.viewer.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampHelper {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    public static String getTimestamp() {
        return getTimestamp(new Date());
    }

    public static String getTimestamp(Date date) {
        return simpleDateFormat.format(date);
    }

}
