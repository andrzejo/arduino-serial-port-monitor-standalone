package pl.andrzejo.aspm.api;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.api.commands.CloseDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.api.commands.OpenDeviceEvent;

public class RestApiService {
    private final ApplicationEventBus eventBus;

    public RestApiService() {
        eventBus = ApplicationEventBus.instance();
    }

    public void start() {
        SimpleHttpServer server = new SimpleHttpServer();
        server.addEndpoint("open", this::handleOpen);
        server.addEndpoint("close", this::handleClose);
    }

    private String handleClose(String body) {
        eventBus.post(new CloseDeviceEvent());
        return null;
    }

    private String handleOpen(String body) {
        eventBus.post(new OpenDeviceEvent(body));
        return null;
    }

}
