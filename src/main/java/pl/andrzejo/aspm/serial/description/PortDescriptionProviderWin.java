/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.serial.description;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PortDescriptionProviderWin extends DescriptionProviderCommand {
    private static final String Command = "Get-WMIObject -Query \\\"SELECT Caption FROM Win32_PnPEntity WHERE Caption LIKE '%(COM%' \\\" | findstr 'Caption ' ";

    @Override
    protected Map<String, String> getDescriptions(List<String> ports, List<String> descriptions) {
        return ports.stream().collect(Collectors.toMap(Function.identity(), p -> getDesc(p, descriptions)));
    }

    @Override
    protected String getCommand(List<String> ports) {
        return Command;
    }

    private String getDesc(String port, List<String> descriptions) {
        for (String s : descriptions) {
            String label = String.format("(%s)", port);
            if (s.contains(label)) {
                String[] split = s.split("Caption\\s+:");
                if (split.length > 1) {
                    return split[1].replace(label, "").trim();
                }
            }
        }
        return "";
    }
}
