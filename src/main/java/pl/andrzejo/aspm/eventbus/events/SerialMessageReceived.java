package pl.andrzejo.aspm.eventbus.events;

public class SerialMessageReceived extends BusMessage {
    private final String value;

    public SerialMessageReceived(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
