package pl.andrzejo.aspm.gui.setting;

import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.guihandlers.CheckBoxSettingsHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

public class RtsSettingHandler extends CheckBoxSettingsHandler {
    public RtsSettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defConfig) {
        super(setting, config, config::setRTS, config.isRTS(), defConfig.isRTS(), true);
    }
}
