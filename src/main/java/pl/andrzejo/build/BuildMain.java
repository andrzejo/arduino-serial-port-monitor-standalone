package pl.andrzejo.build;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BuildMain {

    public static void main(String[] args) throws IOException {
        String increase = System.getProperty("build.increaseVersion");
        if (StringUtils.equalsIgnoreCase(increase, "true")) {
            System.out.println("Increase build version");
            String currentVersion = getArg(args, 0);
            String projectDir = getArg(args, 1);

            System.out.println("Current version: " + currentVersion);
            System.out.println("Project dir: " + projectDir);
            String version = getNewVersion(currentVersion);
            System.out.println("New version: " + version);

            replaceVersion(projectDir, currentVersion, version);
            createVersionBuildFile(projectDir, version);
        }
    }

    private static void createVersionBuildFile(String projectDir, String version) throws IOException {
        File buildFile = Paths.get(projectDir, "src", "main", "resources", "version.properties").toFile();
        String builder = "version=" + version + "\n";
        builder += "build.date=" + getCurrentDate() + "\n";
        FileUtils.writeStringToFile(buildFile, builder, StandardCharsets.UTF_8);
    }

    private static String getCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    private static void replaceVersion(String projectDir, String currentVersion, String version) throws IOException {
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
