package pl.andrzejo.aspm.factory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BeanFactory {
    private static final Map<Class<?>, Object> objects = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T instance(Class<T> type) {
        return (T) objects.computeIfAbsent(type, BeanFactory::createInstance);
    }

    @SuppressWarnings("unchecked")
    public static <T> T instance(Class<T> type, Supplier<T> factory) {
        return (T) objects.computeIfAbsent(type, (k) -> factory.get());
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

    private static Object createInstance(Class<?> k) {
        try {
            for (Constructor<?> constructor : k.getDeclaredConstructors()) {
                if (constructor.getParameterCount() == 0) {
                    constructor.setAccessible(true);
                    return constructor.newInstance();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + k.getName(), e);
        }

        throw new RuntimeException("Instantiation failed. Class has no default constructor " + k.getName() + "");
    }
}
