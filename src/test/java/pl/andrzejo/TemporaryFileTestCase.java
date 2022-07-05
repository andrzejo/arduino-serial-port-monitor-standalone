package pl.andrzejo;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

public class TemporaryFileTestCase {
    protected File tmpPath;

    @BeforeEach
    void baseSetUp() {
        tmpPath = Paths.get(FileUtils.getTempDirectoryPath(), "aspm-tmp").toFile();
        tmpPath.mkdirs();
    }

    @AfterEach
    void baseTearDown() {
        FileUtils.deleteQuietly(tmpPath);
    }

    protected File copyFromResource(String resourceName, String destName) throws IOException {
        File file = new File(tmpPath, destName);
        file.getParentFile().mkdirs();
        FileUtils.copyFile(getResourceFile(resourceName), file);
        return file;
    }

    protected File getResourceFile(String resourceName) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL resource = classloader.getResource(resourceName);
        Objects.requireNonNull(resource, "Failed to open resource file: " + resourceName);
        return new File(resource.getFile());
    }
}
