/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus;

import pl.andrzejo.aspm.eventbus.events.BusEvent;
import pl.andrzejo.aspm.eventbus.impl.EventBus;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;

import java.util.List;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class ApplicationEventBus {
    private final EventBus eventBus;

    public ApplicationEventBus() {
        eventBus = instance(EventBus.class);
    }

    public void register(Object listener) {
        eventBus.registerListener(listener);
    }

    public void post(BusEvent msg) {
        eventBus.trigger(msg);
    }

    public List<Object> postForResult(BusEvent msg) {
        return eventBus.triggerAndWaitForResults(msg);
    }

    public <T> T postForSingleResult(BusEvent msg) {
        List<Object> results = eventBus.triggerAndWaitForResults(msg);
        if (results.size() > 1) {
            throw new IllegalStateException("More than one result returned for " + msg.getClass().getSimpleName());
        }
        if (results.isEmpty()) {
            return null;
        }
        return (T) results.get(0);
    }

    public void post(AppSetting<?> msg) {
        eventBus.trigger(msg);
    }
}
