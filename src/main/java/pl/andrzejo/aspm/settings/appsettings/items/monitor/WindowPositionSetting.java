/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings.items.monitor;

import pl.andrzejo.aspm.settings.types.RectSetting;

import java.awt.*;

public class WindowPositionSetting extends GroupMonitorSetting<Rectangle> {

    public static final Rectangle DEF_VALUE = new Rectangle(10, 10, 400, 400);

    protected WindowPositionSetting() {
        super(RectSetting.class, "windowSize", DEF_VALUE);
    }

}
