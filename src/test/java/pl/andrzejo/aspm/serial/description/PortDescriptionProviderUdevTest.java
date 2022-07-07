package pl.andrzejo.aspm.serial.description;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import pl.andrzejo.TemporaryFileTestCase;
import pl.andrzejo.aspm.factory.BeanFactory;
import pl.andrzejo.aspm.utils.SystemExec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PortDescriptionProviderUdevTest extends TemporaryFileTestCase {

    @Test
    void shouldGetDescFromUdevAdm() throws IOException {
        //given
        List<String> output = FileUtils.readLines(getResourceFile("serial.description/udevadm.output"), StandardCharsets.UTF_8);
        PortDescriptionProviderUdev provider = new PortDescriptionProviderUdev();
        SystemExec exec = mock(SystemExec.class);
        when(exec.exec(anyString())).thenReturn(output);
        BeanFactory.overrideInstance(SystemExec.class, exec);

        //when
        Map<String, String> map = provider.get(Arrays.asList("/dev/ttyUSB0", "/dev/input/mouse0"));

        //then
        assertThat(map)
                .hasSize(2)
                .containsEntry("/dev/input/mouse0", "Dell_Universal_Receiver")
                .containsEntry("/dev/ttyUSB0", "CH340 serial converter");
    }
}
