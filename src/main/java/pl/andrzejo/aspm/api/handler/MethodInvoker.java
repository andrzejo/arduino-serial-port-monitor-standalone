/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.api.handler;

import pl.andrzejo.aspm.api.Request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker {
    private final Object handler;
    private final Method handlerMethod;
    private final boolean passRequest;
    private final boolean returnStr;

    public MethodInvoker(Object handler, Method handlerMethod) {
        this.handler = handler;
        this.handlerMethod = handlerMethod;
        this.returnStr = handlerMethod.getReturnType().equals(String.class);
        this.passRequest = handlerMethod.getParameterCount() == 1 && handlerMethod.getParameterTypes()[0].equals(Request.class);
    }

    public String invoke(Request request) {
        try {
            Object result = passRequest
                    ? handlerMethod.invoke(handler, request)
                    : handlerMethod.invoke(handler);

            return returnStr ? (String) result : null;

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
