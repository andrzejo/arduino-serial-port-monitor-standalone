package pl.andrzejo.aspm.settings.appsettings.items.monitor;

import pl.andrzejo.aspm.settings.types.BoolSetting;

public class AutoscrollSetting extends GroupMonitorSetting<Boolean> {
    public AutoscrollSetting() {
        super(BoolSetting.class, "autoScroll", false);
    }
}
