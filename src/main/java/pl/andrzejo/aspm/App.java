/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.andrzejo.aspm.utils.AppFiles;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class App {
    public static String Name = "Arduino Serial Port Monitor - Standalone";
    public static String ConfigDir = ".arduino-serial-port-monitor-st";
    public static Ver Version;
    public static String GitHubUrl = "https://github.com/andrzejo/arduino-serial-port-monitor-standalone";

    static {
        Version = readVersion();
    }

    private static Ver readVersion() {
        String notAvailable = "N/A";
        try {
            try (InputStream stream = AppFiles.resourceAsStream("version.properties")) {
                Properties props = new Properties();
                props.load(stream);
                String version = props.getProperty("version", notAvailable);
                String date = props.getProperty("build.date", notAvailable);
                return new Ver(version, date);
            }
        } catch (Exception e) {
            //
        }
        return new Ver(notAvailable, notAvailable);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Ver {
        private final String ver;
        private final String date;

        public String getYear() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
            return String.valueOf(dateTime.getYear());
        }

    }

}
