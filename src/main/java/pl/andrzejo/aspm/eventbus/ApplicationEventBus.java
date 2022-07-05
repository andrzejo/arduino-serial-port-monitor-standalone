package pl.andrzejo.aspm.eventbus;

import pl.andrzejo.aspm.eventbus.events.BusEvent;
import pl.andrzejo.aspm.eventbus.impl.EventBus;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;

public class ApplicationEventBus {
    private final EventBus eventBus;

    public ApplicationEventBus() {
        eventBus = new EventBus();
    }

    public void register(Object listener) {
        eventBus.registerListener(listener);
    }

    public void post(BusEvent msg) {
        eventBus.trigger(msg);
    }

    public void post(AppSetting<?> msg) {
        eventBus.trigger(msg);
    }
}
