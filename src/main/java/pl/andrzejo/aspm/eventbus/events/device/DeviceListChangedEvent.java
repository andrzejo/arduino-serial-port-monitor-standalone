package pl.andrzejo.aspm.eventbus.events.device;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

import java.util.List;

public class DeviceListChangedEvent extends BusEvent {
    private final List<String> devices;

    public DeviceListChangedEvent(List<String> devices) {
        this.devices = devices;
    }

    public List<String> getDevices() {
        return devices;
    }
}
