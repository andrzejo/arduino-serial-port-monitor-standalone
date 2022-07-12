/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.serial.description;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.utils.SystemExec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public abstract class DescriptionProviderCommand implements DescriptionProvider {
    private static final Logger logger = LoggerFactory.getLogger(DescriptionProviderCommand.class);
    protected abstract Map<String, String> getDescriptions(List<String> devices, List<String> descriptions);

    protected abstract String getCommand(List<String> ports);

    @Override
    public Map<String, String> get(List<String> ports) {
        if (ports.isEmpty()) {
            return new HashMap<>();
        }
        String command = getCommand(ports);
        List<String> output = instance(SystemExec.class).exec(command);
        logger.debug("{}: {}", command, output);
        return getDescriptions(ports, output);
    }

}
