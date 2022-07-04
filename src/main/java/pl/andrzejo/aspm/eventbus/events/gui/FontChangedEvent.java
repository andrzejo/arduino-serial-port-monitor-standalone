package pl.andrzejo.aspm.eventbus.events.gui;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class FontChangedEvent extends BusEvent {
    private final String name;

    public FontChangedEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
