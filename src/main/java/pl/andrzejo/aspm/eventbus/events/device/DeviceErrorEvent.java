package pl.andrzejo.aspm.eventbus.events.device;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class DeviceErrorEvent extends BusEvent {
    private String message;

    public DeviceErrorEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
