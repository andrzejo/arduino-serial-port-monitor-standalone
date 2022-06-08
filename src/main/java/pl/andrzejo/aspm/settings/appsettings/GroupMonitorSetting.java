package pl.andrzejo.aspm.settings.appsettings;

import pl.andrzejo.aspm.settings.Setting;

public class GroupMonitorSetting<T> extends AppSetting<T> {
    public GroupMonitorSetting(Class<? extends Setting<T>> setting, String name, T defValue) {
        super(setting, "monitor", name, defValue);
    }
}
