package pl.andrzejo.aspm.settings;

import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.Properties;

public class SettingsRepository {
    private static final SettingsRepository inst = new SettingsRepository();
    private final Properties props;
    private final File path;

    public SettingsRepository() {
        props = new Properties();
        path = getSettingsPath();
        read();
    }

    private File getSettingsPath() {
        return new File(FileUtils.getUserDirectory(), ".arduino-serial-port-monitor-st.properties");
    }

    public static SettingsRepository instance() {
        return inst;
    }

    public void saveRect(String key, Rectangle rec) {
        saveObject(key, rec);
    }

    public void saveInt(String key, int value) {
        saveObject(key, value);
    }

    public Rectangle getRect(String key, Rectangle def) {
        return readObject(key, def);
    }

    private void saveString(String key, String value) {
        props.setProperty(key, value);
        save();
    }

    private void saveObject(String key, Serializable object) {
        try {
            props.setProperty(key, Serializer.serialize(object));
            save();
        } catch (IOException e) {
            System.err.println("Failed to save " + key + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    private <T> T readObject(String key, T def) {
        try {
            String property = props.getProperty(key);
            return Serializer.deserialize(property);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    private void read() {
        try {
            props.load(Files.newInputStream(path.toPath()));
        } catch (IOException e) {
            //ignore missing file
        }
    }

    public void save() {
        try {
            props.store(Files.newOutputStream(path.toPath()), "Arduino Serial Port Monitor - Standalone settings");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
