/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.serial;


import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SerialPorts {

    public List<Port> getList() {
        return Arrays.stream(SerialPort.getCommPorts())
                .map(p -> new Port(p.getSystemPortPath(), p.getDescriptivePortName()))
                .collect(Collectors.toList());
    }

    @Getter
    @RequiredArgsConstructor
    public static class Port {
        private final String name;
        private final String desc;
    }

}
