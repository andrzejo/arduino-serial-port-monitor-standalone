package pl.andrzejo.aspm.eventbus.events;

public class SerialMessageReceivedEvent extends BusEvent {
    private final String value;

    public SerialMessageReceivedEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
