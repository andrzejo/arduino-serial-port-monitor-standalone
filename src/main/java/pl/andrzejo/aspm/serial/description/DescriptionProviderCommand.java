package pl.andrzejo.aspm.serial.description;

import pl.andrzejo.aspm.utils.SystemExec;

import java.util.List;
import java.util.Map;

public abstract class DescriptionProviderCommand implements DescriptionProvider {
    private final String command;

    public DescriptionProviderCommand(String command) {
        this.command = command;
    }

    protected abstract Map<String, String> getDescriptions(List<String> devices, List<String> descriptions);

    @Override
    public Map<String, String> get(List<String> ports) {
        List<String> output = SystemExec.exec(command);
        return getDescriptions(ports, output);
    }

}
