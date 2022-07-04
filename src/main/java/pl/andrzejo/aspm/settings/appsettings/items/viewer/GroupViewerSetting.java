package pl.andrzejo.aspm.settings.appsettings.items.viewer;

import pl.andrzejo.aspm.settings.Setting;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;

public class GroupViewerSetting<T> extends AppSetting<T> {

    protected GroupViewerSetting(Class<? extends Setting<T>> setting, String name, T defValue) {
        super(setting, "viewer", name, defValue);
    }

}
