package pl.andrzejo.aspm.api;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiCloseDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiExecuteCommand;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiOpenDeviceEvent;
import pl.andrzejo.aspm.serial.SerialPorts;
import pl.andrzejo.aspm.service.SerialHandlerService;

import java.util.List;
import java.util.function.Function;

import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Get;
import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Post;

public class RestApiService {
    private static RestApiService inst;
    private final ApplicationEventBus eventBus;

    private RestApiService() {
        eventBus = ApplicationEventBus.instance();
    }

    public static RestApiService instance() {
        if (inst == null) {
            inst = new RestApiService();
        }
        return inst;
    }

    public static String getStatusEndpointAddress() {
        return SimpleHttpServer.getAddress() + "/api/status";
    }

    public void start() {
        SimpleHttpServer server = new SimpleHttpServer();
        setupEndpoint(server, Post, "open", this::handleOpen);
        setupEndpoint(server, Post, "close", this::handleClose);
        setupEndpoint(server, Get, "status", this::handleStatus);
        setupEndpoint(server, Get, "devices", this::handleDevices);
    }

    private void setupEndpoint(SimpleHttpServer server, SimpleHttpServer.Method method, String path, Function<String, String> handler) {
        server.addEndpoint(method, path, (body) -> {
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

    private String handleStatus(String body) {
        SerialHandlerService.Status status = SerialHandlerService.instance().getStatus();
        return status.toHumanReadableString();
    }

    private String handleDevices(String body) {
        List<String> list = SerialPorts.getList();
        return String.join("\n", list);
    }
}
