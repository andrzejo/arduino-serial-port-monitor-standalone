/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static pl.andrzejo.aspm.eventbus.impl.MethodDescription.getDescription;

public class EventBus {
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);

    private final Map<Class<?>, List<HandlerMethod>> handlers = new ConcurrentHashMap<>();

    public void registerListener(Object listener) {
        Objects.requireNonNull(listener, "EventBus listener must not be null");
        extractListenersFromObject(listener);
    }

    public List<Object> triggerAndWaitForResults(Object event) {
        if (event == null) {
            return Collections.emptyList();
        }
        Class<?> type = event.getClass();
        List<HandlerMethod> handlerMethods = handlers.get(type);
        if (handlerMethods == null) {
            return Collections.emptyList();
        }
        LinkedList<Object> results = new LinkedList<>();
        handlerMethods.forEach((m) -> {
            try {
                logger.debug("Handle event {}({})", m.handlerMethodName(), event);
                Object value = m.invoke(event);
                if (value != null) {
                    results.add(value);
                }
            } catch (Exception e) {
                String error = String.format("EventBus event handler (%s) thrown exception.", m.handlerDescription());
                logger.error(error, e);
            }
        });
        return results;
    }

    public void trigger(Object event) {
        triggerAndWaitForResults(event);
    }

    private void extractListenersFromObject(Object listener) {
        for (Method m : listener.getClass().getDeclaredMethods()) {
            if (m.getDeclaredAnnotation(Subscribe.class) != null) {
                if (m.getParameterCount() != 1) {
                    throw new EventBusException(String.format("EventBus handler method (%s) must have only one parameter", getDescription(m)));
                }
                Class<?> type = m.getParameterTypes()[0];
                addHandler(type, new HandlerMethod(listener, m));
            }
        }
    }

    private void addHandler(Class<?> type, HandlerMethod handlerMethod) {
        List<HandlerMethod> typeHandlers = handlers.computeIfAbsent(type, k -> new ArrayList<>());
        typeHandlers.add(handlerMethod);
    }

    public Map<Class<?>, List<HandlerMethod>> getHandlers() {
        return handlers;
    }

}
