package pl.andrzejo.aspm.settings.appsettings.items.device;

import pl.andrzejo.aspm.settings.types.StringSetting;

public class LastDeviceSetting extends GroupDeviceSetting<String> {
    protected LastDeviceSetting() {
        super(StringSetting.class, "lastDevice", null);
    }
}