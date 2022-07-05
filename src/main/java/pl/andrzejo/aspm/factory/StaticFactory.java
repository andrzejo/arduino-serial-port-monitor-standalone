package pl.andrzejo.aspm.factory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class StaticFactory {
    private static final Map<Class<?>, Object> objects = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T instance(Class<T> type) {
        return (T) objects.computeIfAbsent(type, StaticFactory::createInstance);
    }

    public static void overrideInstance(Class<?> type, Object instance) {
        objects.put(type, instance);
    }

    private static Object createInstance(Class<?> k) {
        try {
            for (Constructor<?> constructor : k.getConstructors()) {
                if (constructor.getParameterCount() == 0) {
                    return constructor.newInstance();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + k.getName(), e);
        }

        throw new RuntimeException("Instantiation failed. Class has no default constructor " + k.getName() + "");
    }

}
