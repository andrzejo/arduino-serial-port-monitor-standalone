/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.andrzejo.aspm.eventbus.events.BusEvent;
import pl.andrzejo.aspm.eventbus.impl.EventBus;
import pl.andrzejo.aspm.factory.BeanFactory;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;
import pl.andrzejo.aspm.settings.types.StringSetting;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ApplicationEventBusTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = mock(EventBus.class);
        BeanFactory.overrideInstance(EventBus.class, eventBus);
    }

    @Test
    void shouldPostEvent() {
        //given
        ApplicationEventBus bus = new ApplicationEventBus();
        BusEvent event = new BusEvent();

        //when
        bus.post(event);

        //then
        verify(eventBus).trigger(event);
    }

    @Test
    void shouldPostEvent2() {
        //given
        ApplicationEventBus bus = new ApplicationEventBus();
        SomeSetting setting = new SomeSetting();

        //when
        bus.post(setting);

        //then
        verify(eventBus).trigger(setting);
    }

    @Test
    void shouldSetupListener() {
        //given
        ApplicationEventBus bus = new ApplicationEventBus();
        BusEvent event = new BusEvent();

        //when
        bus.register(this);

        //then
        verify(eventBus).registerListener(this);
    }

    static class SomeSetting extends AppSetting<String> {

        protected SomeSetting() {
            super(StringSetting.class, "some-setting-group", "some-setting", "");
        }
    }
}
