package pl.andrzejo.aspm.api;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiCloseDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiExecuteCommand;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiOpenDeviceEvent;
import pl.andrzejo.aspm.serial.SerialPorts;
import pl.andrzejo.aspm.service.SerialHandlerService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Get;
import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Post;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class AppApiService {
    private final ApplicationEventBus eventBus;
    private final List<Endpoint> endpoints = new ArrayList<>();
    private final ApiIndex apiIndex;

    public AppApiService() {
        apiIndex = new ApiIndex();
        eventBus = instance(ApplicationEventBus.class);
    }

    public static String getRootEndpointAddress() {
        return SimpleHttpServer.getAddress();
    }

    public void start() {
        SimpleHttpServer server = instance(SimpleHttpServer.class);
        setupEndpoint(server, Post, "/api/open", this::handleOpen, "Open device. Specify device in request body. If device is not specified opens first selected.");
        setupEndpoint(server, Post, "/api/close", this::handleClose, "Close device.");
        setupEndpoint(server, Get, "/api/status", this::handleStatus, "Get device status.");
        setupEndpoint(server, Get, "/api/devices", this::handleDevices, "Get available devices.");
        setupEndpoint(server, Get, null, this::handleRoot, "Get endpoints.");
    }

    private void setupEndpoint(SimpleHttpServer server, SimpleHttpServer.Method method, String path, Function<String, String> handler, String desc) {
        endpoints.add(new Endpoint(method, path, desc));
        server.addEndpoint(method, path, (body) -> {
            if (path != null) {
                eventBus.post(new ApiExecuteCommand(path, body));
            }
            return handler.apply(body);
        });
    }

    private String handleRoot(String s) {
        return apiIndex.getHtml(endpoints);
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
        SerialHandlerService.Status status = instance(SerialHandlerService.class).getStatus();
        return status.toHumanReadableString();
    }

    private String handleDevices(String body) {
        List<String> list = SerialPorts.getList();
        return String.join("\n", list);
    }

    public static class Endpoint {
        private final String path;
        private final String desc;
        private final SimpleHttpServer.Method method;

        public Endpoint(SimpleHttpServer.Method method, String path, String desc) {
            this.method = method;
            this.path = path;
            this.desc = desc;
        }

        public String getPath() {
            return path;
        }

        public String getDesc() {
            return desc;
        }

        public SimpleHttpServer.Method getMethod() {
            return method;
        }
    }
}
