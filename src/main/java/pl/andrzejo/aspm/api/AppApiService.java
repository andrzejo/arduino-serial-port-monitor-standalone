/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.api;

import org.apache.commons.lang.BooleanUtils;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiCloseDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiExecuteCommand;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiOpenDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.gui.BringWindowToTopEvent;
import pl.andrzejo.aspm.eventbus.events.gui.ClearMonitorOutputEvent;
import pl.andrzejo.aspm.eventbus.events.gui.GetMonitorOutputEvent;
import pl.andrzejo.aspm.serial.SerialPorts;
import pl.andrzejo.aspm.service.SerialHandlerService;
import pl.andrzejo.aspm.utils.OsInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.contains;
import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Get;
import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Post;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class AppApiService {
    private final ApplicationEventBus eventBus;
    private final List<Endpoint> endpoints = new ArrayList<>();
    private final ApiIndex apiIndex;

    public AppApiService() {
        apiIndex = instance(ApiIndex.class);
        eventBus = instance(ApplicationEventBus.class);
    }

    public static String getRootEndpointAddress() {
        return SimpleHttpServer.getAddress();
    }

    public void start() {
        SimpleHttpServer server = instance(SimpleHttpServer.class);
        builder()
                .method(Post)
                .path("/api/device/open")
                .handler(this::handleOpen)
                .description("Open device. Specify device in request body. If device is not specified opens first selected.")
                .bodyExample(getBodyExample())
                .build(server);

        builder()
                .method(Post)
                .path("/api/device/close")
                .handler(this::handleClose)
                .description("Close device.")
                .build(server);

        builder()
                .method(Get)
                .path("/api/device/status")
                .handler(this::handleStatus)
                .description("Get device status.")
                .build(server);

        builder()
                .method(Get)
                .path("/api/device/list")
                .handler(this::handleDevices)
                .description("Get available devices.")
                .build(server);

        builder()
                .method(Get)
                .path("/api/window/focus")
                .handler(this::handleWindowFocus)
                .description("Bring app window to top.")
                .build(server);

        builder()
                .method(Post)
                .path("/api/monitor/clear")
                .handler(this::handleMonitorOutputClear)
                .description("Clear monitor output.")
                .build(server);

        builder()
                .method(Get)
                .path("/api/monitor/output")
                .handler(this::handleGetMonitorOutput)
                .description("Get monitor output.")
                .queryParams("with_messages")
                .build(server);

        builder()
                .method(Get)
                .handler(this::handleRoot)
                .description("Get endpoints.")
                .build(server);
    }

    private Builder builder() {
        return new Builder();
    }

    private static String getBodyExample() {
        if (OsInfo.CurrentOs == OsInfo.OsName.Windows) {
            return "COM1";
        }
        return "/dev/ttyUSB0";
    }

    private String handleRoot(Request request) {
        return apiIndex.getHtml(endpoints);
    }

    private String handleMonitorOutputClear(Request request) {
        eventBus.post(new ClearMonitorOutputEvent());
        return null;
    }

    private String handleGetMonitorOutput(Request request) {
        boolean withMessages = contains(request.getRequestURI().getQuery(), "with_messages");
        List<Object> objects = eventBus.postForResult(new GetMonitorOutputEvent(withMessages));
        return objects.stream().map(Object::toString).collect(Collectors.joining());
    }

    private String handleWindowFocus(Request request) {
        eventBus.post(new BringWindowToTopEvent(BooleanUtils.toBoolean(request.getBody())));
        return null;
    }

    private String handleClose(Request request) {
        eventBus.post(new ApiCloseDeviceEvent());
        return null;
    }

    private String handleOpen(Request request) {
        eventBus.post(new ApiOpenDeviceEvent(request.getBody()));
        return null;
    }

    private String handleStatus(Request request) {
        SerialHandlerService.Status status = instance(SerialHandlerService.class).getStatus();
        return status.toHumanReadableString();
    }

    private String handleDevices(Request request) {
        List<String> list = instance(SerialPorts.class).getList();
        return String.join("\n", list);
    }

    public static class EndpointDescription {
        private final String desc;
        private final String bodyExample;

        public String getQueryParams() {
            return queryParams;
        }

        public String getBodyExample() {
            return bodyExample;
        }

        public String getDesc() {
            return desc;
        }

        private final String queryParams;

        public EndpointDescription(String desc, String bodyExample, String queryParams) {
            this.desc = desc;
            this.bodyExample = bodyExample;
            this.queryParams = queryParams;
        }
    }

    public static class Endpoint {
        private final String path;
        private final EndpointDescription description;
        private final SimpleHttpServer.Method method;

        public Endpoint(SimpleHttpServer.Method method, String path, EndpointDescription description) {
            this.method = method;
            this.path = path;
            this.description = description;
        }

        public String getPath() {
            return path;
        }

        public SimpleHttpServer.Method getMethod() {
            return method;
        }

        public EndpointDescription getDescription() {
            return description;
        }
    }

    private class Builder {
        private String path;
        private SimpleHttpServer.Method method;
        private String description;
        private String bodyExample;
        private String queryParams;
        private Function<Request, String> handler;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder method(SimpleHttpServer.Method method) {
            this.method = method;
            return this;
        }

        public Builder handler(Function<Request, String> handler) {
            this.handler = handler;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder bodyExample(String bodyExample) {
            this.bodyExample = bodyExample;
            return this;
        }

        public Builder queryParams(String queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public void build(SimpleHttpServer server) {
            endpoints.add(new Endpoint(method, path, new EndpointDescription(description, bodyExample, queryParams)));
            server.addEndpoint(method, path, (request) -> {
                if (method == Post) {
                    eventBus.post(new ApiExecuteCommand(path, request.getBody()));
                }
                return handler.apply(request);
            });
        }
    }
}
