package pl.andrzejo.aspm.settings.appsettings;

import pl.andrzejo.aspm.settings.types.BoolSetting;

public class AutoscrollSetting extends GroupMonitorSetting<Boolean> {
    public AutoscrollSetting() {
        super(BoolSetting.class, "autoScroll", false);
    }
}
