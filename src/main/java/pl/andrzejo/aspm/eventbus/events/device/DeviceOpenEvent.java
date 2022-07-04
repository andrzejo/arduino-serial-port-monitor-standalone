package pl.andrzejo.aspm.eventbus.events.device;

import pl.andrzejo.aspm.eventbus.events.BusEvent;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

public class DeviceOpenEvent extends BusEvent {
    private final DeviceConfig config;

    public DeviceOpenEvent(DeviceConfig config) {
        super();
        this.config = config;
    }

    public DeviceConfig getConfig() {
        return config;
    }
}
