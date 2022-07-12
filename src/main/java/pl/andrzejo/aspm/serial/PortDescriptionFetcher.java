/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.serial.description.DescriptionProvider;
import pl.andrzejo.aspm.serial.description.PortDescriptionProviderUdev;
import pl.andrzejo.aspm.serial.description.PortDescriptionProviderWin;
import pl.andrzejo.aspm.utils.OsInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PortDescriptionFetcher {
    private static final Logger logger = LoggerFactory.getLogger(PortDescriptionFetcher.class);
    private final DescriptionProvider descriptionProvider;

    public PortDescriptionFetcher() {
        Map<OsInfo.OsName, Supplier<DescriptionProvider>> suppliers = new HashMap<OsInfo.OsName, Supplier<DescriptionProvider>>() {{
            put(OsInfo.OsName.Linux, PortDescriptionProviderUdev::new);
            put(OsInfo.OsName.Windows, PortDescriptionProviderWin::new);
            put(OsInfo.OsName.Other, () -> ports -> new HashMap<>());
        }};

        descriptionProvider = suppliers.get(OsInfo.CurrentOs).get();
    }

    public Map<String, String> fetch(List<String> p) {
        try {
            return descriptionProvider.get(p);
        } catch (Exception e) {
            logger.warn("Device description fetch failed.", e);
        }
        return new HashMap<>();
    }
}
