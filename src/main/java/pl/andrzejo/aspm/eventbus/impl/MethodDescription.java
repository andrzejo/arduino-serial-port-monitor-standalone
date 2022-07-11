/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.eventbus.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MethodDescription {

    public static String getDescription(Method method) {
        String args = Arrays
                .stream(method.getParameters())
                .map(p -> String.format("%s %s", p.getParameterizedType().getTypeName(), p.getName()))
                .collect(Collectors.joining(", "));
        return String.format("%s::%s(%s)", method.getDeclaringClass().getCanonicalName(), method.getName(), args);
    }

}
