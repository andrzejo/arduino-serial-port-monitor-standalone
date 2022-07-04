package pl.andrzejo.aspm.settings.appsettings.items.monitor;

import pl.andrzejo.aspm.settings.Setting;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;

public class GroupMonitorSetting<T> extends AppSetting<T> {
    public GroupMonitorSetting(Class<? extends Setting<T>> setting, String name, T defValue) {
        super(setting, "monitor", name, defValue);
    }
}
