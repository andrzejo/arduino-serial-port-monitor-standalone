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
import pl.andrzejo.aspm.gui.viewer.ViewerApiBridge;

import static org.apache.commons.lang.StringUtils.contains;
import static pl.andrzejo.aspm.api.server.SimpleHttpServer.Method.Post;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;

@SuppressWarnings("unused")
public class MonitorApi extends AbstractApiHandler {
    private final ViewerApiBridge apiBridge = instance(ViewerApiBridge.class);

    @ApiEndpoint(
            description = "Get monitor output.",
            queryParams = "with_messages",
            order = 1
    )
    public String output(Request request) {
        boolean withMessages = contains(request.getRequestURI().getQuery(), "with_messages");
        return apiBridge.getOutput(withMessages);
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
