package pl.andrzejo.aspm.gui.setting;

import pl.andrzejo.aspm.settings.appsettings.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.guihandlers.ListSettingHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.util.LinkedHashMap;


public class ParitySettingHandler extends ListSettingHandler<Character> {
    public ParitySettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defValue) {
        super(setting, config, config::setParity, config::getParity, defValue.getParity(), true);
    }

    @Override
    protected void fillItems(LinkedHashMap<Character, String> items) {
        items.put('N', "none");
        items.put('E', "even");
        items.put('O', "odd");
    }
}
