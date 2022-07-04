package pl.andrzejo.aspm.gui.setting;

import pl.andrzejo.aspm.settings.appsettings.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.guihandlers.ListSettingHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.util.LinkedHashMap;

public class BaudSettingHandler extends ListSettingHandler<Integer> {
    protected static Integer[] serialRates = {300, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 74880, 115200, 230400, 250000, 500000, 1000000, 2000000};

    public BaudSettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defValue) {
        super(setting, config, config::setBaud, config::getBaud, defValue.getBaud(), false);
    }

    @Override
    protected void fillItems(LinkedHashMap<Integer, String> items) {
        for (Integer rate : serialRates) {
            items.put(rate, rate.toString());
        }
    }
}
