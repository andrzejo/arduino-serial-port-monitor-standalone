package pl.andrzejo.aspm.eventbus.events.gui;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class FontChangedEvent extends BusEvent {
    private final String name;
    private final Integer size;

    public FontChangedEvent(String name, Integer size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public Integer getSize() {
        return size;
    }
}
