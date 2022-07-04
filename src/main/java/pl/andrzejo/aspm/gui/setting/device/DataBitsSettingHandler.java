package pl.andrzejo.aspm.gui.setting.device;

import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.guihandlers.ListSettingHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.util.LinkedHashMap;

public class DataBitsSettingHandler extends ListSettingHandler<DeviceConfig, Integer> {

    public DataBitsSettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defValue) {
        super(setting, config, config::setDataBits, config::getDataBits, defValue.getDataBits(), true);
    }

    @Override
    protected void fillItems(LinkedHashMap<Integer, String> items) {
        items.put(5, "5");
        items.put(6, "6");
        items.put(7, "7");
        items.put(8, "8");
    }
}
