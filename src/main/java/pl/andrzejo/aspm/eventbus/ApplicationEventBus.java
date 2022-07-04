package pl.andrzejo.aspm.eventbus;

import com.google.common.eventbus.EventBus;
import pl.andrzejo.aspm.eventbus.events.BusEvent;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;

public class ApplicationEventBus {
    private static final ApplicationEventBus instance = new ApplicationEventBus();
    private final EventBus eventBus;

    public static ApplicationEventBus instance() {
        return instance;
    }

    public ApplicationEventBus() {
        eventBus = new EventBus();
    }

    public void register(Object listener) {
        eventBus.register(listener);
    }

    public void post(BusEvent msg) {
        eventBus.post(msg);
    }

    public void post(AppSetting<?> msg) {
        eventBus.post(msg);
    }
}
