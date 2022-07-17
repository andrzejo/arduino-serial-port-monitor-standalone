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
        apiIndex = instance(ApiIndex.class);
        eventBus = instance(ApplicationEventBus.class);
    }

    public static String getRootEndpointAddress() {
        return SimpleHttpServer.getAddress();
    }

    public void start() {
        SimpleHttpServer server = instance(SimpleHttpServer.class);
        setupEndpoint(server, Post, "/api/device/open", this::handleOpen, "Open device. Specify device in request body. If device is not specified opens first selected.");
        setupEndpoint(server, Post, "/api/device/close", this::handleClose, "Close device.");
        setupEndpoint(server, Get, "/api/device/status", this::handleStatus, "Get device status.");
        setupEndpoint(server, Get, "/api/device/list", this::handleDevices, "Get available devices.");

        setupEndpoint(server, Post, "/api/window/focus", this::handleWindowFocus, "Bring app window to top.");
        setupEndpoint(server, Get, null, this::handleRoot, "Get endpoints.");
    }

    private void setupEndpoint(SimpleHttpServer server, SimpleHttpServer.Method method, String path, Function<String, String> handler, String desc) {
        endpoints.add(new Endpoint(method, path, desc));
        server.addEndpoint(method, path, (body) -> {
            if (method == Post) {
                eventBus.post(new ApiExecuteCommand(path, body));
            }
            return handler.apply(body);
        });
    }

    private String handleRoot(String s) {
        return apiIndex.getHtml(endpoints);
    }

    private String handleWindowFocus(String body) {
        eventBus.post(new BringWindowToTopEvent(BooleanUtils.toBoolean(body)));
        return null;
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
        List<String> list = instance(SerialPorts.class).getList();
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
