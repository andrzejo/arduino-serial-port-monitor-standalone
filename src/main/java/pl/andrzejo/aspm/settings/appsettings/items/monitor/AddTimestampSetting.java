package pl.andrzejo.aspm.settings.appsettings.items.monitor;

import pl.andrzejo.aspm.settings.types.BoolSetting;

public class AddTimestampSetting extends GroupMonitorSetting<Boolean> {
    public AddTimestampSetting() {
        super(BoolSetting.class, "addTimestamp", false);
    }
}
