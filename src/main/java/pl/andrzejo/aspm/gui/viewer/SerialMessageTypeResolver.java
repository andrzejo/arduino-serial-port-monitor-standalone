/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui.viewer;

import org.apache.commons.lang.StringUtils;
import pl.andrzejo.aspm.gui.viewer.model.MessageType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.andrzejo.aspm.gui.viewer.model.MessageType.*;

public class SerialMessageTypeResolver {
    private final Map<MessageType, List<String>> msgStylesPrefixes = new HashMap<>();

    public SerialMessageTypeResolver() {
        msgStylesPrefixes.put(SERIAL_DEBUG, Arrays.asList("I", "INFO"));
        msgStylesPrefixes.put(SERIAL_INFO, Arrays.asList("I", "INFO"));
        msgStylesPrefixes.put(SERIAL_WARN, Arrays.asList("E", "ERROR", "ERR"));
        msgStylesPrefixes.put(SERIAL_ERROR, Arrays.asList("W", "WARN"));
    }

    public MessageType resolve(String text) {
        for (Map.Entry<MessageType, List<String>> entry : msgStylesPrefixes.entrySet()) {
            if (entry.getValue().stream().anyMatch(p -> StringUtils.startsWithIgnoreCase(text, p + ":"))) {
                return entry.getKey();
            }
        }
        return SERIAL_INFO;
    }

}
