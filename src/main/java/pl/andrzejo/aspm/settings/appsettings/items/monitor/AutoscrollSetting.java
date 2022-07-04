package pl.andrzejo.aspm.settings.appsettings.items.monitor;

import pl.andrzejo.aspm.settings.types.BoolSetting;

public class AutoscrollSetting extends GroupMonitorSetting<Boolean> {
    protected AutoscrollSetting() {
        super(BoolSetting.class, "autoScroll", false);
    }
}
