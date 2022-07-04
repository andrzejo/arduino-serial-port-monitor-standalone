package pl.andrzejo.aspm.utils;

import java.io.File;
import java.net.URL;
import java.util.Objects;

public class Files {

    public static File fromResource(String path) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = Objects.requireNonNull(classloader.getResource(path), "Resource file '" + path + "' not found.");
        return new File(url.getFile());
    }

}
