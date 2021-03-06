/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
        String root = FileUtils.getUserDirectoryPath();
        File file = new File(root, App.ConfigDir);
        file.mkdirs();
        return file;
    }

    public static String readResources(String path) {
        try {
            return IOUtils.toString(resourceAsStream(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
