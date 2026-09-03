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
import pl.andrzejo.aspm.eventbus.events.gui.ClearMonitorOutputEvent;
import pl.andrzejo.aspm.eventbus.events.gui.GetMonitorOutputEvent;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.contains;
import static pl.andrzejo.aspm.api.server.SimpleHttpServer.Method.Post;

@SuppressWarnings("unused")
public class MonitorApi extends AbstractApiHandler {

    @ApiEndpoint(
            description = "Get monitor output.",
            queryParams = "with_messages",
            order = 1
    )
    public String output(Request request) {
        boolean withMessages = contains(request.getRequestURI().getQuery(), "with_messages");
        List<Object> objects = eventBus.postForResult(new GetMonitorOutputEvent(withMessages));
        return objects.stream().map(Object::toString).collect(Collectors.joining());
    }

    @ApiEndpoint(
            method = Post,
            description = "Clear monitor output.",
            order = 2
    )
    public void clear() {
        eventBus.post(new ClearMonitorOutputEvent());
    }
}
