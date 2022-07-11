/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.appsettings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Debouncer<T> {
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final int millis;
    private final Consumer<AppSetting<T>> handler;
    private long nextExecTimestamp;
    private AppSetting<T> current;

    public Debouncer(Consumer<AppSetting<T>> handler, int millis) {
        this.handler = handler;
        this.millis = millis;
        executor.scheduleWithFixedDelay(this::run, 0, 10, TimeUnit.MILLISECONDS);
    }

    private void run() {
        if (nextExecTimestamp != 0 && nextExecTimestamp < System.currentTimeMillis()) {
            nextExecTimestamp = 0;
            handler.accept(current);
        }
    }

    public void debounce(AppSetting<T> data) {
        current = data;
        nextExecTimestamp = System.currentTimeMillis() + millis;
    }
}
