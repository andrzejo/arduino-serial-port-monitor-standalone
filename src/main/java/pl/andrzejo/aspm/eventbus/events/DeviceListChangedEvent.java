package pl.andrzejo.aspm.eventbus.events;

import java.util.List;

public class DeviceListChangedEvent extends BusEvent {
    private List<String> devices;

    public DeviceListChangedEvent(List<String> devices) {
        this.devices = devices;
    }

    public List<String> getDevices() {
        return devices;
    }
}
