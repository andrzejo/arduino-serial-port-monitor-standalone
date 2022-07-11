/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.impl;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EventBusTest {

    @Test
    void shouldRegisterHandlers() {
        //given
        EventBus bus = new EventBus();
        SomeHandler handler = new SomeHandler();

        //when
        bus.registerListener(handler);

        //then
        Map<Class<?>, List<HandlerMethod>> handlers = bus.getHandlers();
        assertThat(handlers).hasSize(2);
        assertThat(handlers.get(String.class)).map(HandlerMethod::handlerDescription).contains("pl.andrzejo.aspm.eventbus.impl.EventBusTest.SomeHandler::handle(java.lang.String arg0)");
        assertThat(handlers.get(Event.class)).map(HandlerMethod::handlerDescription).contains("pl.andrzejo.aspm.eventbus.impl.EventBusTest.SomeHandler::handle(pl.andrzejo.aspm.eventbus.impl.EventBusTest$Event arg0)");
    }

    @Test
    void shouldRegisterHandleEvent() {
        //given
        EventBus bus = new EventBus();
        SomeHandler handler = new SomeHandler();
        bus.registerListener(handler);

        //when
        bus.trigger(new Event("Some data"));
        bus.trigger("Some string1");
        bus.trigger("Some string2");

        //then
        List<Object> invocations = handler.getInvocations();
        assertThat(invocations)
                .hasSize(3)
                .map(Object::toString)
                .contains("Event{param='Some data'}", "Some string1", "Some string2");
    }

    static class SomeHandler {
        List<Object> invocations = new ArrayList<>();

        @Subscribe
        @SuppressWarnings("unused")
        public void handle(String event) {
            invocations.add(event);
        }

        @Subscribe
        @SuppressWarnings("unused")
        private int handle(Event event) {
            invocations.add(event);
            return 0;
        }

        public List<Object> getInvocations() {
            return invocations;
        }
    }

    static class Event {
        private String param;

        public Event(String param) {
            this.param = param;
        }

        @Override
        public String toString() {
            return "Event{" +
                    "param='" + param + '\'' +
                    '}';
        }
    }
}
