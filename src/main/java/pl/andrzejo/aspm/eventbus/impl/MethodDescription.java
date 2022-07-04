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
