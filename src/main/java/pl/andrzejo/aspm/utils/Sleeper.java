/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.utils;

public class Sleeper {

    public static void sleep(long millis) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            //
        }
    }

}
