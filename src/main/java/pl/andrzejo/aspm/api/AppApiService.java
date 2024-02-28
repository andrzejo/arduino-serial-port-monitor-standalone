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
import pl.andrzejo.aspm.utils.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        setupEndpoint(server, Post, "/api/device/open", this::handleOpen,
                "Open device. Specify device in request body. If device is not specified opens first selected.",
                getBodyExample());
        setupEndpoint(server, Post, "/api/device/close", this::handleClose, "Close device.");
        setupEndpoint(server, Get, "/api/device/status", this::handleStatus, "Get device status.");
        setupEndpoint(server, Get, "/api/device/list", this::handleDevices, "Get available devices.");

        setupEndpoint(server, Post, "/api/window/focus", this::handleWindowFocus, "Bring app window to top.");
        setupEndpoint(server, Post, "/api/monitor/clear", this::handleMonitorOutputClear, "Clear monitor output.");
        setupEndpoint(server, Get, "/api/monitor/output", this::handleGetMonitorOutput, "Get monitor output. Optional add ?with_messages parameters for full output.");
        setupEndpoint(server, Get, null, this::handleRoot, "Get endpoints.");
    }

    private static String getBodyExample() {
        if (OsInfo.CurrentOs == OsInfo.OsName.Windows) {
            return "COM1";
        }
        return "/dev/ttyUSB0";
    }

    private void setupEndpoint(SimpleHttpServer server,
                               SimpleHttpServer.Method method,
                               String path,
                               Function<Request, String> handler,
                               String desc,
                               String bodyExample) {
        endpoints.add(new Endpoint(method, path, desc, bodyExample));
        server.addEndpoint(method, path, (request) -> {
            if (method == Post) {
                eventBus.post(new ApiExecuteCommand(path, request.getBody()));
            }
            return handler.apply(request);
        });
    }

    private void setupEndpoint(SimpleHttpServer server,
                               SimpleHttpServer.Method method,
                               String path,
                               Function<Request, String> handler,
                               String desc) {
        setupEndpoint(server, method, path, handler, desc, null);
    }

    private String handleRoot(Request request) {
        return apiIndex.getHtml(endpoints);
    }

    private String handleMonitorOutputClear(Request request) {
        eventBus.post(new ClearMonitorOutputEvent());
        return null;
    }

    private String handleGetMonitorOutput(Request request) {
        boolean withMessages = StrUtil.contains(request.getRequestURI().getQuery(), "with_messages");
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

    public static class Endpoint {
        private final String path;
        private final String desc;
        private final String bodyExample;
        private final SimpleHttpServer.Method method;

        public Endpoint(SimpleHttpServer.Method method, String path, String desc, String bodyExample) {
            this.method = method;
            this.path = path;
            this.desc = desc;
            this.bodyExample = bodyExample;
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

        public String getBodyExample() {
            return bodyExample;
        }
    }
}
