package pl.andrzejo.aspm.settings.appsettings;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.settings.Setting;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;

public class AppSetting<T> {
    private final Setting<T> setting;
    private final Debouncer<T> debouncer;
    private final ApplicationEventBus eventBus;

    public AppSetting(Class<? extends Setting<T>> settingType, String component, String name, T def) {
        String key = createKey(component, name);
        setting = createSettingInstance(key, settingType, def);
        debouncer = new Debouncer<T>(this::sendEvent, 500);
        eventBus = ApplicationEventBus.instance();
        debouncer.debounce(this);
    }

    private static String createKey(String component, String name) {
        return component + "." + name;
    }

    @SuppressWarnings("unchecked")
    private Setting<T> createSettingInstance(String key, Class<? extends Setting<T>> settingType, T def) {
        Optional<Constructor<?>> constructor = Arrays
                .stream(settingType.getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == 2)
                .findFirst();
        if (constructor.isPresent()) {
            Constructor<?> constr = constructor.get();
            try {
                return (Setting<T>) constr.newInstance(key, def);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Failed to create setting item " + settingType.getCanonicalName());
        }
    }

    public T get() {
        return setting.get();
    }

    public void set(T value) {
        T last = setting.get();
        setting.set(value);
        if (last != null && !last.equals(value)) {
            debouncer.debounce(this);
        }
    }

    private void sendEvent(AppSetting<T> o) {
        eventBus.post(o);
    }

}
