/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.api.handler;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.utils.Serializer;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public abstract class AbstractApiHandler {
    protected final ApplicationEventBus eventBus;
    protected final Serializer serializer;

    protected AbstractApiHandler() {
        eventBus = instance(ApplicationEventBus.class);
        serializer = instance(Serializer.class);
    }

    public String getBasePath() {
        return "";
    }
}
