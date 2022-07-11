/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SystemExec {
    public enum OsName {
        Linux, Windows, Other
    }

    public final static OsName CurrentOs;

    static {
        String osName = System.getProperty("os.name");
        if (osName.equals("Linux")) CurrentOs = OsName.Linux;
        else if (osName.startsWith("Win")) {
            CurrentOs = OsName.Windows;
        } else {
            CurrentOs = OsName.Other;
        }
    }

    public List<String> exec(String command) {
        try {
            List<String> args = getCommandLine(command);
            ProcessBuilder builder = new ProcessBuilder(args);
            Process proc = builder.start();

            List<String> output = readOutput(proc.getInputStream());
            proc.waitFor(5, TimeUnit.SECONDS);
            int exitVal = proc.exitValue();
            if (exitVal != 0) {
                String error = String.join("\n", readOutput(proc.getErrorStream()));
                throw new RuntimeException(String.format("Command '%s' failed with exit code %d. Output: %s", command, exitVal, error));
            }
            return output;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> readOutput(InputStream stream) throws IOException {
        StringBuilder b = new StringBuilder();
        ArrayList<String> lines = new ArrayList<>();
        while (true) {
            int sign = stream.read();
            char c = (char) sign;
            if (sign == -1) {
                addLine(b, lines);
                break;
            }
            b.append(c);
            if (c == '\n') {
                addLine(b, lines);
                b.setLength(0);
            }
        }
        return lines;
    }

    private void addLine(StringBuilder b, ArrayList<String> lines) {
        String line = b.toString().trim();
        if (line.length() > 0) {
            lines.add(line);
        }
    }

    private List<String> getCommandLine(String command) {
        switch (CurrentOs) {
            case Linux:
                return Arrays.asList("bash", "-c", command);
            case Windows:
                return Arrays.asList("powershell.exe", "-Command", command);
            default:
                return Collections.emptyList();
        }

    }
}
