package pl.andrzejo.aspm.settings.appsettings.items.device;

import pl.andrzejo.aspm.settings.appsettings.items.device.GroupDeviceSetting;
import pl.andrzejo.aspm.settings.types.DeviceConfig;
import pl.andrzejo.aspm.settings.types.DeviceSetting;

public class TtyDeviceSetting extends GroupDeviceSetting<DeviceConfig> {

    public TtyDeviceSetting() {
        super(DeviceSetting.class, "tty", DeviceConfig.defaultConfig());
    }
}
