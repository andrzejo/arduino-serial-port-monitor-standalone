/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

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
