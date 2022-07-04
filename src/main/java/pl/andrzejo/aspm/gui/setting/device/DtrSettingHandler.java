package pl.andrzejo.aspm.gui.setting.device;

import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.guihandlers.CheckBoxSettingsHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

public class DtrSettingHandler extends CheckBoxSettingsHandler {

    public DtrSettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defConfig) {
        super(setting, config, config::setDTR, config.isDTR(), defConfig.isDTR(), true);
    }

}
