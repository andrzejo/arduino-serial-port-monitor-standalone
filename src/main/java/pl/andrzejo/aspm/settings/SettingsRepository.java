package pl.andrzejo.aspm.settings;

import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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

    public void saveRect(String key, Rectangle rec) {
        saveInt(key + ".x", rec.x);
        saveInt(key + ".y", rec.y);
        saveInt(key + ".w", rec.width);
        saveInt(key + ".h", rec.height);
    }

    public Rectangle getRect(String key, Rectangle def) {
        int x = getInt(key + ".x", def.x);
        int y = getInt(key + ".y", def.y);
        int w = getInt(key + ".w", def.width);
        int h = getInt(key + ".h", def.height);
        return new Rectangle(x, y, w, h);
    }

    public void saveInt(String key, int value) {
        saveString(key, String.valueOf(value));
    }

    public int getInt(String key, int defValue) {
        return readOrDefault(defValue, () -> {
            String value = getString(key, String.valueOf(defValue));
            return Integer.valueOf(value);
        });
    }

    private void saveString(String key, String value) {
        props.setProperty(key, value);
        isDirty = true;
    }

    private String getString(String key, String defValue) {
        return props.getProperty(key, defValue);
    }

    private <T> T readOrDefault(T defValue, Supplier<T> converter) {
        try {
            return converter.get();
        } catch (Exception e) {
            return defValue;
        }
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
        if (isDirty) {
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
