/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.app.ApplicationClosingEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.gui.viewer.model.Message;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.SaveLogToFile;
import pl.andrzejo.aspm.utils.AppFiles;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class OutputLogger {
    private static final Logger log = LoggerFactory.getLogger(OutputLogger.class);

    private final SaveLogToFile saveLogToFile;
    private final File outputFile;
    @Getter
    private static final File logFile = new File(AppFiles.getAppConfigDir(), "aspm.log.txt");
    private final ExecutorService diskWriterExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "LogFileWriter-Thread");
        t.setDaemon(true);
        return t;
    });

    public OutputLogger() {
        this(logFile, AppSettingsFactory.create(SaveLogToFile.class));
        instance(ApplicationEventBus.class).register(this);
    }

    @SneakyThrows
    OutputLogger(File outputFile, SaveLogToFile saveLogToFile) {
        this.outputFile = outputFile;
        this.saveLogToFile = saveLogToFile;
        FileUtils.forceMkdir(outputFile.getAbsoluteFile().getParentFile());
    }

    public void log(List<Message> messages) {
        if (messages == null || messages.isEmpty() || !saveLogToFile.get()) {
            return;
        }
        List<Message> batch = new ArrayList<>(messages);
        try {
            diskWriterExecutor.submit(() -> writeBatchToFile(batch));
        } catch (RejectedExecutionException e) {
            log.warn("Failed to write batch to file, executor is shutting down");
        }
    }

    private void writeBatchToFile(List<Message> batch) {
        try (BufferedWriter writer = createWriter()) {
            for (Message msg : batch) {
                writer.write('[');
                writer.write(msg.getFormattedTimestamp());
                writer.write("] ");
                writer.write(msg.getText());
                writer.newLine();
            }
            writer.flush();
        } catch (Exception e) {
            log.error("Failed to save output log: {}", e.getMessage(), e);
        }
    }

    private BufferedWriter createWriter() throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile, true), StandardCharsets.UTF_8), 16 * 1024);
    }

    public void shutdown() {
        diskWriterExecutor.shutdown();
        try {
            if (!diskWriterExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                log.warn("Timed out waiting for pending log writes");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for pending log writes");
        }
    }

    @Subscribe(priority = 20)
    @SuppressWarnings("unused")
    private void handleEvent(ApplicationClosingEvent event) {
        log.info("Shutting down output logger");
        shutdown();
    }

}
