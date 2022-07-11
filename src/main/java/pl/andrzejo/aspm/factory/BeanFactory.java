/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.factory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BeanFactory {
    private static final Map<Class<?>, Object> objects = new HashMap<>();

    public static <T> T instance(Class<T> type) {
        return instance(type, () -> createInstance(type));
    }

    @SuppressWarnings("unchecked")
    public static <T> T instance(Class<T> type, Supplier<T> factory) {
        synchronized (objects) {
            Object inst = objects.get(type);
            if (inst == null) {
                inst = factory.get();
                objects.put(type, inst);
            }
            return (T) inst;
        }
    }

    public static <T> void overrideInstance(Class<T> type, T instance) {
        objects.put(type, instance);
    }

    public static void reset() {
        objects.clear();
    }

    public static void clearInstance(Class<?> type) {
        objects.remove(type);
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInstance(Class<?> k) {
        try {
            for (Constructor<?> constructor : k.getDeclaredConstructors()) {
                if (constructor.getParameterCount() == 0) {
                    constructor.setAccessible(true);
                    return (T) constructor.newInstance();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + k.getName(), e);
        }

        throw new RuntimeException("Instantiation failed. Class has no default constructor " + k.getName() + "");
    }
}
