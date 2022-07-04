package pl.andrzejo.aspm.eventbus.events;

import java.util.Date;

public class SerialMessageReceivedEvent extends BusEvent {
    private final String value;
    private final Date date;

    public SerialMessageReceivedEvent(String value) {
        this.value = value;
        date = new Date();
    }

    public String getValue() {
        return value;
    }

    public Date getDate() {
        return date;
    }
}
