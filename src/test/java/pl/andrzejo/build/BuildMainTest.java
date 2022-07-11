/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.build;

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

    @Test
    void shouldDoNothingIfPropertyNotSet() throws IOException {
        //given
        File pom = copyFromResource("test-versioning-pom.xml", "pom.xml");

        //when
        BuildMain.main(new String[]{"1.1.1", tmpPath.getAbsolutePath()});

        //then
        File expected = getResourceFile("test-versioning-pom.xml");
        assertThat(expected).hasSameTextualContentAs(pom);
    }

    @Test
    void shouldReplaceVersion() throws IOException, ParseException {
        //given
        BeanFactory.overrideInstance(Date.class, TestUtils.createDate("2022-01-01 11:11:11"));

        System.setProperty("build.increaseVersion", "true");
        File pom = copyFromResource("test-versioning-pom.xml", "pom.xml");

        //when
        BuildMain.main(new String[]{"1.0.1", tmpPath.getAbsolutePath()});

        //then
        File versionFile = Paths.get(tmpPath.getAbsolutePath(), "src", "main", "resources", "version.properties").toFile();
        assertThat(pom).content().contains("<version>1.0.2</version>");
        assertThat(versionFile).content()
                .contains("version=1.0.2")
                .contains("build.date=2022-01-01 11:11:11");
    }
}
