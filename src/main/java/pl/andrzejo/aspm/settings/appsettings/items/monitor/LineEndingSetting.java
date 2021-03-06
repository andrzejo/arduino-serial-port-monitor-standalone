/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings.items.monitor;

import pl.andrzejo.aspm.settings.types.StringSetting;

public class LineEndingSetting extends GroupMonitorSetting<String> {

    public static final String DEF_VALUE = "\n";

    protected LineEndingSetting() {
        super(StringSetting.class, "lineEnding", DEF_VALUE);
    }

}
