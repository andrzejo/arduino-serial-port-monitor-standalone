/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.serial.description;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PortDescriptionProviderWinTest {

    @Test
    void shouldParseDesc() {
        //given
        PortDescProvider provider = new PortDescProvider();
        List<String> ports = Arrays.asList("COM4", "COM3");
        List<String> desc = Arrays.asList("Caption          : Intel(R) Active Management Technology - SOL (COM3)", "Caption          : USB to Serial (RS-232/DB9) (COM4)");

        //when
        Map<String, String> descriptions = provider.getDescriptions(ports, desc);

        //then
        assertThat(descriptions)
                .containsEntry("COM4", "USB to Serial (RS-232/DB9)")
                .containsEntry("COM3", "Intel(R) Active Management Technology - SOL");
    }

    private static class PortDescProvider extends PortDescriptionProviderWin {
        @Override
        protected Map<String, String> getDescriptions(List<String> ports, List<String> descriptions) {
            return super.getDescriptions(ports, descriptions);
        }
    }
}
