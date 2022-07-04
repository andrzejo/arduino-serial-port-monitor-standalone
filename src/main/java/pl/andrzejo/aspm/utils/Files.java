package pl.andrzejo.aspm.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Files {

    public static File fromResource(String path) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = Objects.requireNonNull(classloader.getResource(path), "Resource file '" + path + "' not found.");
        return new File(url.getFile());
    }

    public static void write(String path, String content) {
        try {
            FileUtils.writeStringToFile(new File(path), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
