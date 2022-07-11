/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.serial.description;

import java.util.List;
import java.util.Map;

public interface DescriptionProvider {
    Map<String, String> get(List<String> ports);
}
