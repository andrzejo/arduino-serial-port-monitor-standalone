package pl.andrzejo.aspm.eventbus.events.command;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class CommandExecutedEvent extends BusEvent {
    private final String command;

    public CommandExecutedEvent(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
