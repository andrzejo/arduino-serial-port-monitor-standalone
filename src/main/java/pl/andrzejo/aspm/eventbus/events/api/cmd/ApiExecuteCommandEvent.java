/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.eventbus.events.api.cmd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.andrzejo.aspm.eventbus.events.BusEvent;

@Getter
@RequiredArgsConstructor
public class ApiExecuteCommandEvent extends BusEvent {
    private final String command;
}
