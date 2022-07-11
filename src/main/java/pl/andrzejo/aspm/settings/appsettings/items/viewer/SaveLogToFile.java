/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings.items.viewer;

import pl.andrzejo.aspm.settings.types.BoolSetting;

public class SaveLogToFile extends GroupViewerSetting<Boolean> {

    protected SaveLogToFile() {
        super(BoolSetting.class, "saveLogToFile", false);
    }
}
