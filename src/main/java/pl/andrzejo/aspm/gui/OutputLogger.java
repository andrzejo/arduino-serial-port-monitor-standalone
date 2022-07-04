package pl.andrzejo.aspm.gui;

import org.apache.commons.io.FileUtils;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.SaveLogToFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class OutputLogger {

    private final SaveLogToFile saveLogToFile;
    private final File logFile;

    public OutputLogger() {
        saveLogToFile = AppSettingsFactory.create(SaveLogToFile.class);
        logFile = new File(FileUtils.getUserDirectory(), "arduino-serial-port-monitor-st.log.txt");
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
}
