package pl.andrzejo.aspm.gui;

import org.apache.commons.io.FileUtils;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.SaveLogToFile;
import pl.andrzejo.aspm.utils.AppFiles;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class OutputLogger {
    private final SaveLogToFile saveLogToFile;
    private static final File logFile = new File(AppFiles.getAppConfigDir(), "aspm.log.txt");

    public OutputLogger() {
        saveLogToFile = AppSettingsFactory.create(SaveLogToFile.class);
    }

    public void log(String text) {
        if (saveLogToFile.get()) {
            try {
                FileUtils.writeStringToFile(logFile, text, StandardCharsets.UTF_8, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static File getLogFile() {
        return logFile;
    }
}
