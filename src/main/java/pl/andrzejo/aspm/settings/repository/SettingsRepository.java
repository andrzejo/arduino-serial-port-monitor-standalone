package pl.andrzejo.aspm.settings.repository;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SettingsRepository {
    private static final SettingsRepository inst = new SettingsRepository();
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
        return new File(FileUtils.getUserDirectory(), ".arduino-serial-port-monitor-st.properties");
    }

    public static SettingsRepository instance() {
        return inst;
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
