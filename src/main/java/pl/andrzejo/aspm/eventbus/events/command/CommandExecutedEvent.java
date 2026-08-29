/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.events.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.andrzejo.aspm.eventbus.events.BusEvent;
import pl.andrzejo.aspm.gui.cmd.CommandItem;

@Getter
@RequiredArgsConstructor
public class CommandExecutedEvent extends BusEvent {
    private final CommandItem command;
    private final String lineEnding;
}
