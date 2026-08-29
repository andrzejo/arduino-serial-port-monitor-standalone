/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.events.device;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.andrzejo.aspm.eventbus.events.BusEvent;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

@Getter
@RequiredArgsConstructor
public class DeviceOpenEvent extends BusEvent {
    private final DeviceConfig config;
}
