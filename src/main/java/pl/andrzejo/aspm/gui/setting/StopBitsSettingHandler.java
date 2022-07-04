package pl.andrzejo.aspm.gui.setting;

import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.guihandlers.ListSettingHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.util.LinkedHashMap;

public class StopBitsSettingHandler extends ListSettingHandler<DeviceConfig, Float> {

    public StopBitsSettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defConfig) {
        super(setting, config, config::setStopBits, config::getStopBits, defConfig.getStopBits(), true);
    }

    @Override
    protected void fillItems(LinkedHashMap<Float, String> items) {
        items.put(1f, "1");
        items.put(1.5f, "1.5");
        items.put(2f, "2");
    }
}
