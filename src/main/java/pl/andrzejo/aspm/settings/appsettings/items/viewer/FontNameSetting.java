/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings.items.viewer;

import pl.andrzejo.aspm.settings.types.StringSetting;

public class FontNameSetting extends GroupViewerSetting<String> {
    protected FontNameSetting() {
        super(StringSetting.class, "fontName", "Monospaced");
    }
}
