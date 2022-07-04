package pl.andrzejo.aspm.eventbus.events.api.commands;

import pl.andrzejo.aspm.eventbus.events.api.ApiCommandEvent;

public class OpenDeviceEvent extends ApiCommandEvent {
    private final String device;

    public OpenDeviceEvent(String device) {
        this.device = device;
    }

    public String getDevice() {
        return device;
    }
}
