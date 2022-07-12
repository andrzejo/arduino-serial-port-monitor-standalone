/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.serial.description;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isBlank;

public class PortDescriptionProviderUdev extends DescriptionProviderCommand {
    private static final String Command = "sleep 1; udevadm info";

    @Override
    protected Map<String, String> getDescriptions(List<String> devices, List<String> descriptions) {
        Map<String, Map<String, String>> entries = toEntries(descriptions);
        return devices.stream().collect(Collectors.toMap(Function.identity(), p -> getDesc(p, entries)));
    }

    private String getDesc(String device, Map<String, Map<String, String>> entries) {
        Map<String, String> props = entries.get(device);
        if (props != null) {
            return firstNotEmpty(props.get("ID_MODEL_FROM_DATABASE"), props.get("ID_MODEL"));
        }
        return "";
    }

    private String firstNotEmpty(String... args) {
        return Arrays.stream(args).filter(StringUtils::isNotEmpty).findFirst().orElse("");
    }

    private Map<String, Map<String, String>> toEntries(List<String> descriptions) {
        Map<String, Map<String, String>> entries = new HashMap<>();
        Map<String, String> current = new HashMap<>();
        descriptions.add("");
        descriptions.forEach(l -> {
            if (l.startsWith("E:")) {
                String[] parts = l.replace("E: ", "").split("=");
                if (parts.length > 1) {
                    current.put(parts[0], parts[1]);
                }
            }
            if (isBlank(l)) {
                String device = current.get("DEVNAME");
                entries.put(device, new HashMap<>(current));
                current.clear();
            }
        });
        return entries;
    }

    @Override
    protected String getCommand(List<String> ports) {
        return String.format("%s %s", Command, String.join(" ", ports));
    }
}
