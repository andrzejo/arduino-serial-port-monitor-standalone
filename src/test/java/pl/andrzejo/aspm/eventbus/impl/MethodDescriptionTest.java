/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.impl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class MethodDescriptionTest {

    @Test
    void shouldGetMethodDescription() throws NoSuchMethodException {
        //given
        Method method = getClass().getDeclaredMethod("handler", String.class);

        //when
        String description = MethodDescription.getDescription(method);

        //then
        assertThat(description).isEqualTo("pl.andrzejo.aspm.eventbus.impl.MethodDescriptionTest::handler(java.lang.String arg0)");
    }

    private void handler(String event) {

    }
}
