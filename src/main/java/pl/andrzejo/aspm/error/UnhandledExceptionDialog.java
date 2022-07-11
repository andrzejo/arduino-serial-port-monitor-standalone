/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.error;

import pl.andrzejo.aspm.gui.MainWindowContainer;

import javax.swing.*;

public class UnhandledExceptionDialog {
    public boolean confirmExit(Throwable e, String trace) {
        JOptionPane.showMessageDialog(MainWindowContainer.getComponent(), e.getMessage() + "\nTrace:\n" + trace, "Unexpected error", JOptionPane.ERROR_MESSAGE);
        return true;
    }
}
