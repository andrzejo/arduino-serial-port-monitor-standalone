package pl.andrzejo.aspm.gui.setting;

import com.google.common.eventbus.Subscribe;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.DeviceListChangedEvent;
import pl.andrzejo.aspm.settings.appsettings.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.guihandlers.ListSettingHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.util.LinkedHashMap;
import java.util.List;

public class DeviceSettingHandler extends ListSettingHandler<String> {

    public DeviceSettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defValue) {
        super(setting, config, config::setDevice, config::getDevice, defValue.getDevice(), false);
        ApplicationEventBus.instance().register(this);
    }

    @Override
    protected void fillItems(LinkedHashMap<String, String> items) {
    }

    @Subscribe
    public void handleEvent(DeviceListChangedEvent event) {
        setNewValues(toMap(event.getDevices()));
    }

    private LinkedHashMap<String, String> toMap(List<String> devices) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        devices.forEach(s -> map.put(s, s));
        return map;
    }
}
