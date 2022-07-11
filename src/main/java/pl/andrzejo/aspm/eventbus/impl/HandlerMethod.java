/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HandlerMethod {
    private final Object listener;
    private final Method method;

    public HandlerMethod(Object listener, Method method) {
        this.listener = listener;
        this.method = method;
    }

    public void invoke(Object event) {
        try {
            method.setAccessible(true);
            method.invoke(listener, event);
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
