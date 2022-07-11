/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.error;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class DefaultErrorHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultErrorHandler.class);
    private final UnhandledExceptionDialog dialog;

    private boolean isShown = false;

    public DefaultErrorHandler() {
        dialog = instance(UnhandledExceptionDialog.class);
    }

    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Unhandled exception caught!", e);
        if (!isShown) {
            isShown = true;
            String trace = ExceptionUtils.getStackTrace(e);
            if (dialog.confirmExit(e, trace)) {
                System.exit(55);
            }
        }
    }

}
