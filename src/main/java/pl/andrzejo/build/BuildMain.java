/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.build;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import pl.andrzejo.aspm.factory.BeanFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class BuildMain {

    public static void main(String[] args) throws IOException {
        String newVersion = System.getProperty("build.setVersion");
        if (isNotBlank(newVersion)) {
            validateNewVersion(newVersion);
            System.out.println("Set project version to " + newVersion);
            String currentVersion = getArg(args, 0);
            String projectDir = getArg(args, 1);

            System.out.println("Current version: " + currentVersion);
            System.out.println("Project dir: " + projectDir);

            replacePomVersion(projectDir, currentVersion, newVersion);
            createVersionBuildFile(projectDir, newVersion);
            replaceLinuxLauncherVersion(projectDir, newVersion);
            replaceWindowsLauncherVersion(projectDir, newVersion);
        }
    }

    private static void validateNewVersion(String newVersion) {
        if (!newVersion.matches("\\d+\\.\\d+\\.\\d+")) {
            throw new RuntimeException("Invalid version specified: " + newVersion + ". Version should be like x.x.x");
        }
    }

    private static void replaceWindowsLauncherVersion(String projectDir, String version) throws IOException {
        File csproj = Paths.get(projectDir, "launcher", "win", "Launcher", "Launcher.csproj").toFile();
        String xml = FileUtils.readFileToString(csproj, StandardCharsets.UTF_8);
        String newXml = xml.replaceFirst("<AssemblyVersion>.*</AssemblyVersion>", "<AssemblyVersion>" + version + "</AssemblyVersion>");
        FileUtils.writeStringToFile(csproj, newXml, StandardCharsets.UTF_8);
    }

    private static void replaceLinuxLauncherVersion(String projectDir, String version) throws IOException {
        File launcherSh = Paths.get(projectDir, "launcher", "linux", "launcher.sh").toFile();
        String sh = FileUtils.readFileToString(launcherSh, StandardCharsets.UTF_8);
        String replaced = sh.replaceFirst("LAUNCHER_VERSION=.*", "LAUNCHER_VERSION=" + version);
        FileUtils.writeStringToFile(launcherSh, replaced, StandardCharsets.UTF_8);
    }

    private static void createVersionBuildFile(String projectDir, String version) throws IOException {
        File buildFile = Paths.get(projectDir, "src", "main", "resources", "version.properties").toFile();
        String builder = "version=" + version + "\n";
        builder += "build.date=" + getCurrentDate() + "\n";
        FileUtils.writeStringToFile(buildFile, builder, StandardCharsets.UTF_8);
    }

    private static String getCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(BeanFactory.instance(Date.class));
    }

    private static void replacePomVersion(String projectDir, String currentVersion, String version) throws IOException {
        File pom = Paths.get(projectDir, "pom.xml").toFile();
        String xml = FileUtils.readFileToString(pom, StandardCharsets.UTF_8);
        String search = versionPattern(currentVersion);
        String replace = versionPattern(version);
        String newXml = xml.replace(search, replace);
        FileUtils.writeStringToFile(pom, newXml, StandardCharsets.UTF_8);
    }

    private static String versionPattern(String version) {
        return String.format("<version>%s</version>", version);
    }

    private static String getNewVersion(String currentVersion) {
        List<Integer> parts = Arrays.stream(StringUtils.split(currentVersion, "."))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        if (parts.size() != 3) {
            throw new RuntimeException("Invalid version string " + currentVersion + ". Version should be like x.x.x.");
        }
        parts.set(2, parts.get(2) + 1);
        return parts.stream().map(String::valueOf).collect(Collectors.joining("."));
    }

    private static String getArg(String[] args, int idx) {
        if (args.length >= idx) {
            return args[idx];
        }
        throw new RuntimeException("Invalid arguments");
    }
}
