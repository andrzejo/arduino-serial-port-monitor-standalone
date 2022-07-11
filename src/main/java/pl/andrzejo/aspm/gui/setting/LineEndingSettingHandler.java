/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui.setting;

import pl.andrzejo.aspm.settings.appsettings.items.monitor.LineEndingSetting;
import pl.andrzejo.aspm.settings.guihandlers.ListSettingHandler;

import java.util.LinkedHashMap;

public class LineEndingSettingHandler extends ListSettingHandler<String, String> {

    public LineEndingSettingHandler(LineEndingSetting setting) {
        super(setting::set, setting::get, LineEndingSetting.DEF_VALUE, false);
    }

    @Override
    protected void fillItems(LinkedHashMap<String, String> items) {
        items.put("\n", "\\n");
        items.put("\r", "\\r");
        items.put("\n\r", "\\n\\r");
        items.put("", "none");
    }
}
