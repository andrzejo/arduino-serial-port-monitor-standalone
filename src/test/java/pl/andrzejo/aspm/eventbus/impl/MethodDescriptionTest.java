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