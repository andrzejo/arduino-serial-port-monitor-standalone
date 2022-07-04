package pl.andrzejo.aspm.error;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.gui.MainWindowContainer;

import javax.swing.*;

public class DefaultErrorHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultErrorHandler.class);

    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Unhandled exception caught!", e);
        String trace = Throwables.getStackTraceAsString(e);
        JOptionPane.showMessageDialog(MainWindowContainer.getComponent(), e.getMessage() + "\nTrace:\n" + trace, "Unexpected error", JOptionPane.ERROR_MESSAGE);
    }

}
