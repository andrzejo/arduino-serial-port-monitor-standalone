package pl.andrzejo.aspm.eventbus.events.api.commands;

import pl.andrzejo.aspm.eventbus.events.BusEvent;

public class ApiExecuteCommand extends BusEvent {
    private final String command;
    private final String body;

    public ApiExecuteCommand(String command, String body) {
        this.command = command;
        this.body = body;
    }

    public String getCommand() {
        return command;
    }

    public String getBody() {
        return body;
    }
}
