/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.repository;

import pl.andrzejo.aspm.utils.AppFiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SettingsRepository {
    private final Properties props;
    private final File path;
    private boolean isDirty = false;

    public SettingsRepository() {
        props = new Properties();
        path = getSettingsPath();
        read();
        startDirtyWatcher();
    }

    private void startDirtyWatcher() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::save, 0, 1, TimeUnit.SECONDS);
    }

    private File getSettingsPath() {
        return new File(AppFiles.getAppConfigDir(), "aspm.properties");
    }

    public void saveString(String key, String value) {
        props.setProperty(key, value);
        isDirty = true;
    }

    public String getString(String key, String defValue) {
        return props.getProperty(key, defValue);
    }

    private void read() {
        try {
            props.load(Files.newInputStream(path.toPath()));
            isDirty = false;
        } catch (IOException e) {
            //ignore missing file
        }
    }

    public void save() {
        if (!isDirty) {
            return;
        }
        try {
            props.store(Files.newOutputStream(path.toPath()), "Arduino Serial Port Monitor - Standalone settings");
            isDirty = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
