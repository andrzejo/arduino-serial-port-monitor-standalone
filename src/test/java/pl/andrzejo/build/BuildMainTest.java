/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.build;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.andrzejo.TemporaryFileTestCase;
import pl.andrzejo.TestUtils;
import pl.andrzejo.aspm.factory.BeanFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class BuildMainTest extends TemporaryFileTestCase {
    private File pom;
    private File launcherLinux;
    private File launcherWin;

    @BeforeEach
    void setUp() throws IOException, ParseException {
        BeanFactory.overrideInstance(Date.class, TestUtils.createDate("2022-01-01 11:11:11"));
        pom = copyFromResource("build/test-versioning-pom.xml", "pom.xml");
        launcherLinux = copyFromResource("build/launcher.sh", "launcher/linux/launcher.sh");
        launcherWin = copyFromResource("build/Launcher.csproj", "launcher/win/Launcher/Launcher.csproj");
    }

    @Test
    void shouldDoNothingIfPropertyNotSet() throws IOException {
        //given
        //when
        BuildMain.main(new String[]{"1.1.1", tmpPath.getAbsolutePath()});

        //then
        File expected = getResourceFile("build/test-versioning-pom.xml");
        assertThat(expected).hasSameTextualContentAs(pom);
    }

    @Test
    void shouldReplaceVersion() throws IOException, ParseException {
        //given
        System.setProperty("build.increaseVersion", "true");

        //when
        BuildMain.main(new String[]{"1.0.1", tmpPath.getAbsolutePath()});

        //then
        File versionFile = Paths.get(tmpPath.getAbsolutePath(), "src", "main", "resources", "version.properties").toFile();
        assertThat(pom).content().contains("<version>1.0.2</version>");
        assertThat(versionFile).content()
                .contains("version=1.0.2")
                .contains("build.date=2022-01-01 11:11:11");
    }

    @Test
    void shouldReplaceVersionInLauncherLinux() throws IOException {
        //given
        System.setProperty("build.increaseVersion", "true");

        //when
        BuildMain.main(new String[]{"1.0.105", tmpPath.getAbsolutePath()});

        //then
        assertThat(launcherLinux).content().contains("LAUNCHER_VERSION=1.0.106");
    }

    @Test
    void shouldReplaceVersionInLauncherWin() throws IOException {
        //given
        System.setProperty("build.increaseVersion", "true");

        //when
        BuildMain.main(new String[]{"1.0.105", tmpPath.getAbsolutePath()});

        //then
        assertThat(launcherWin).content().contains("<AssemblyVersion>1.0.106</AssemblyVersion>");
    }
}
