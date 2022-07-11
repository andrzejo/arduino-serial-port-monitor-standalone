/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.serial;

import jssc.SerialPortList;

import java.util.Arrays;
import java.util.List;

public class SerialPorts {

    public List<String> getList() {
        return Arrays.asList(SerialPortList.getPortNames());
    }

}
