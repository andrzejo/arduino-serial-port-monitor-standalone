package pl.andrzejo.aspm.settings.appsettings;

import pl.andrzejo.aspm.settings.Setting;

public class GroupDeviceSetting<T> extends AppSetting<T> {
    public GroupDeviceSetting(Class<? extends Setting<T>> setting, String name, T defValue) {
        super(setting, "device", name, defValue);
    }
}
