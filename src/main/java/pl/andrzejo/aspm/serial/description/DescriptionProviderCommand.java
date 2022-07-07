package pl.andrzejo.aspm.serial.description;

import pl.andrzejo.aspm.utils.SystemExec;

import java.util.List;
import java.util.Map;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public abstract class DescriptionProviderCommand implements DescriptionProvider {
    protected abstract Map<String, String> getDescriptions(List<String> devices, List<String> descriptions);

    protected abstract String getCommand(List<String> ports);

    @Override
    public Map<String, String> get(List<String> ports) {
        List<String> output = instance(SystemExec.class).exec(getCommand(ports));
        return getDescriptions(ports, output);
    }

}
