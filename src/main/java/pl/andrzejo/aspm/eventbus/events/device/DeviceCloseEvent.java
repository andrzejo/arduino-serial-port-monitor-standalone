package pl.andrzejo.aspm.eventbus.events.device;

import pl.andrzejo.aspm.eventbus.events.BusEvent;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

public class DeviceCloseEvent extends BusEvent {
    private DeviceConfig config;

    public DeviceCloseEvent(DeviceConfig config) {
        super();
        this.config = config;
    }

    public DeviceConfig getConfig() {
        return config;
    }
}