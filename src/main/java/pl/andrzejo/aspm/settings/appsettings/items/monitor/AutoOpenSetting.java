package pl.andrzejo.aspm.settings.appsettings.items.monitor;

import pl.andrzejo.aspm.settings.types.BoolSetting;

public class AutoOpenSetting extends GroupMonitorSetting<Boolean> {
    protected AutoOpenSetting() {
        super(BoolSetting.class, "autoOpenDevice", false);
    }
}
