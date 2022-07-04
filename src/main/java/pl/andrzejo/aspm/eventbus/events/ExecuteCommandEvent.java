package pl.andrzejo.aspm.eventbus.events;

public class ExecuteCommandEvent extends BusEvent {
    private final String command;
    private final String lineEnding;

    public ExecuteCommandEvent(String command, String lineEnding) {
        this.command = command;
        this.lineEnding = lineEnding;
    }

    public String getCommand() {
        return command;
    }

    public String getLineEnding() {
        return lineEnding;
    }
}
