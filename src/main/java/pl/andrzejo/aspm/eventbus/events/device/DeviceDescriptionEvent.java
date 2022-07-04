package pl.andrzejo.aspm.eventbus.events.device;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

import java.util.Map;

public class DeviceDescriptionEvent extends BusEvent {
    private final Map<String, String> desc;

    public DeviceDescriptionEvent(Map<String, String> desc) {
        this.desc = desc;
    }

    public Map<String, String> getDesc() {
        return desc;
    }
}
