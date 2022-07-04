package pl.andrzejo.aspm.settings.appsettings.items.monitor;

import pl.andrzejo.aspm.settings.appsettings.items.monitor.GroupMonitorSetting;
import pl.andrzejo.aspm.settings.types.BoolSetting;

public class AutoOpenSetting extends GroupMonitorSetting<Boolean> {
    public AutoOpenSetting() {
        super(BoolSetting.class, "autoOpenDevice", false);
    }
}
