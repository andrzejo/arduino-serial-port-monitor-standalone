package pl.andrzejo.aspm.utils;

import org.apache.commons.io.FileUtils;
import pl.andrzejo.aspm.App;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class AppFiles {

    public static InputStream resourceAsStream(String path) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream stream = classloader.getResourceAsStream(path);
        Objects.requireNonNull(stream, "Resource file '" + path + "' not found.");
        return stream;
    }

    public static void write(String path, String content) {
        try {
            FileUtils.writeStringToFile(new File(path), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getAppConfigDir() {
        boolean isWindows = SystemExec.CurrentOs == SystemExec.OsName.Windows;
        String root = isWindows ? System.getenv("APPDATA") : FileUtils.getUserDirectoryPath();
        File file = new File(root, App.ConfigDir);
        file.mkdirs();
        return file;
    }
}
