package pl.andrzejo.aspm.settings.appsettings;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class AppSettingsFactory {
    private static final Map<Object, AppSetting<?>> instances = new HashMap<>();

    public static <T extends AppSetting<?>> T create(Class<T> appSettingType) {
        return (T) instances.computeIfAbsent(appSettingType, (k) -> getInstance(appSettingType));
    }

    private static <T extends AppSetting<?>> AppSetting<?> getInstance(Class<T> appSettingType) {
        Optional<Constructor<?>> constructor = Arrays
                .stream(appSettingType.getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == 0)
                .findFirst();
        if (constructor.isPresent()) {
            Constructor<?> constr = constructor.get();
            try {
                constr.setAccessible(true);
                return (T) constr.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Failed to create app setting " + appSettingType.getCanonicalName());
        }
    }

}
