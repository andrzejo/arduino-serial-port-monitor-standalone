package pl.andrzejo.aspm.api;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiCloseDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiExecuteCommand;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiOpenDeviceEvent;

import java.util.function.Function;

public class RestApiService {
    private final ApplicationEventBus eventBus;

    public RestApiService() {
        eventBus = ApplicationEventBus.instance();
    }

    public void start() {
        SimpleHttpServer server = new SimpleHttpServer();
        setupEndpoint(server, "open", this::handleOpen);
        setupEndpoint(server, "close", this::handleClose);
    }

    private void setupEndpoint(SimpleHttpServer server, String path, Function<String, String> handler) {
        server.addEndpoint(path, (body) -> {
            eventBus.post(new ApiExecuteCommand(path, body));
            return handler.apply(body);
        });
    }

    private String handleClose(String body) {
        eventBus.post(new ApiCloseDeviceEvent());
        return null;
    }

    private String handleOpen(String body) {
        eventBus.post(new ApiOpenDeviceEvent(body));
        return null;
    }

}
