package pl.andrzejo.aspm.eventbus.events.api.commands;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class ApiOpenDeviceEvent extends BusEvent {
    private final String device;

    public ApiOpenDeviceEvent(String device) {
        this.device = device;
    }

    public String getDevice() {
        return device;
    }
}
