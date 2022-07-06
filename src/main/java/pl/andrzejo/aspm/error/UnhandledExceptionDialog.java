package pl.andrzejo.aspm.error;

import pl.andrzejo.aspm.gui.MainWindowContainer;

import javax.swing.*;

public class UnhandledExceptionDialog {
    public boolean confirmExit(Throwable e, String trace) {
        JOptionPane.showMessageDialog(MainWindowContainer.getComponent(), e.getMessage() + "\nTrace:\n" + trace, "Unexpected error", JOptionPane.ERROR_MESSAGE);
        return true;
    }
}
