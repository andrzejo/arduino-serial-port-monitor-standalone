package pl.andrzejo.aspm.eventbus.events.device;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class SelectDeviceEvent extends BusEvent {
    private final String device;

    public SelectDeviceEvent(String device) {
        this.device = device;
    }

    public String getDevice() {
        return device;
    }
}
