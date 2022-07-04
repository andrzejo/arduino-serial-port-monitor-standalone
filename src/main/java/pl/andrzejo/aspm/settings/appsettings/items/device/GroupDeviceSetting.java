package pl.andrzejo.aspm.settings.appsettings.items.device;

import pl.andrzejo.aspm.settings.Setting;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;

public class GroupDeviceSetting<T> extends AppSetting<T> {
    public GroupDeviceSetting(Class<? extends Setting<T>> setting, String name, T defValue) {
        super(setting, "device", name, defValue);
    }
}
