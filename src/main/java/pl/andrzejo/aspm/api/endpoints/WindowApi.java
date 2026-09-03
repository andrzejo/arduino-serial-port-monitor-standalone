/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.api.endpoints;

import org.apache.commons.lang.BooleanUtils;
import pl.andrzejo.aspm.api.Request;
import pl.andrzejo.aspm.api.handler.AbstractApiHandler;
import pl.andrzejo.aspm.api.handler.ApiEndpoint;
import pl.andrzejo.aspm.eventbus.events.gui.BringWindowToTopEvent;

import static pl.andrzejo.aspm.api.server.SimpleHttpServer.Method.Post;

@SuppressWarnings("unused")
public class WindowApi extends AbstractApiHandler {

    @ApiEndpoint(
            method = Post,
            description = "Bring the app window to the top."
    )
    public void focus(Request request) {
        eventBus.post(new BringWindowToTopEvent(BooleanUtils.toBoolean(request.getBody())));
    }

}
