/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui;

import javax.swing.*;
import java.awt.*;

public class MainWindowContainer {
    private static JFrame wnd = null;

    public static void setMainWindowComponent(JFrame window) {
        wnd = window;
    }

    public static JFrame getComponent() {
        return wnd;
    }
}
