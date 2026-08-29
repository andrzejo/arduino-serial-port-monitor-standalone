/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.impl;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class HandlerMethod {
    private final Object listener;
    private final Method method;

    public Object invoke(Object event) {
        try {
            method.setAccessible(true);
            return method.invoke(listener, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public String handlerDescription() {
        return MethodDescription.getDescription(method);
    }

    public String handlerMethodName() {
        return String.format("%s::%s", method.getDeclaringClass().getName(), method.getName());
    }
}
