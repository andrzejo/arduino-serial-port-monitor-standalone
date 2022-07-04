package pl.andrzejo.aspm.error;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.gui.MainWindowContainer;

import javax.swing.*;

public class DefaultErrorHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultErrorHandler.class);

    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Unhandled exception caught!", e);
        String trace = ExceptionUtils.getStackTrace(e);
        JOptionPane.showMessageDialog(MainWindowContainer.getComponent(), e.getMessage() + "\nTrace:\n" + trace, "Unexpected error", JOptionPane.ERROR_MESSAGE);
        System.exit(55);
    }

}
