/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.api.endpoints;


import pl.andrzejo.aspm.api.Request;
import pl.andrzejo.aspm.api.handler.AbstractApiHandler;
import pl.andrzejo.aspm.api.handler.ApiEndpoint;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiCloseDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiOpenDeviceEvent;
import pl.andrzejo.aspm.factory.BeanFactory;
import pl.andrzejo.aspm.serial.SerialPorts;
import pl.andrzejo.aspm.service.SerialHandlerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.andrzejo.aspm.api.server.SimpleHttpServer.Method;

@SuppressWarnings("unused")
public class DeviceApi extends AbstractApiHandler {
    private final SerialHandlerService serialHandlerService = BeanFactory.instance(SerialHandlerService.class);

    @ApiEndpoint(
            method = Method.Post,
            description = "Open device. Specify device in request body. If the device is not specified, opens the first selected.",
            bodyExample = "/dev/ttyUSB0",
            order = 1
    )
    public void open(Request request) {
        eventBus.post(new ApiOpenDeviceEvent(request.getBody()));
    }

    @ApiEndpoint(
            method = Method.Post,
            description = "Close device.",
            order = 2
    )
    public void close() {
        eventBus.post(new ApiCloseDeviceEvent());
    }

    @ApiEndpoint(
            method = Method.Get,
            description = "Get device status.",
            order = 3
    )
    public String status() {
        return serializer.serialize(serialHandlerService.getStatus());
    }

    @ApiEndpoint(
            method = Method.Get,
            description = "Get available devices.",
            order = 4
    )
    public String list() {
        List<SerialPorts.Port> list = serialHandlerService.getDevices();
        Map<String, String> desc = new HashMap<>();
        list.forEach(p -> desc.put(p.getName(), p.getDesc()));
        return serializer.serialize(desc);
    }
}
