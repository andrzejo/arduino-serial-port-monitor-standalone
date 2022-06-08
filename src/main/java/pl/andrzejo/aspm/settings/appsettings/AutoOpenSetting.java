package pl.andrzejo.aspm.settings.appsettings;

import pl.andrzejo.aspm.settings.types.BoolSetting;

public class AutoOpenSetting extends GroupMonitorSetting<Boolean> {
    public AutoOpenSetting() {
        super(BoolSetting.class, "autoOpenDevice", false);
    }
}
