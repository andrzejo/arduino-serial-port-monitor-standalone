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
import pl.andrzejo.aspm.api.server.SimpleHttpServer;
import pl.andrzejo.aspm.eventbus.events.api.cmd.ApiExecuteCommandEvent;
import pl.andrzejo.aspm.gui.cmd.CommandsListApiBridge;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

@SuppressWarnings("unused")
public class CmdApi extends AbstractApiHandler {
    private final CommandsListApiBridge commandApiBridge = instance(CommandsListApiBridge.class);

    @ApiEndpoint(
            description = "List defined commands.",
            order = 1
    )
    public String list(Request request) {
        return serializer.serialize(commandApiBridge.getCommands());
    }

    @ApiEndpoint(
            method = SimpleHttpServer.Method.Post,
            description = "Send command to a connected device.",
            bodyExample = "ECHO TEST",
            order = 2
    )
    public void execute(Request request) {
        String body = request.getBody();
        eventBus.post(new ApiExecuteCommandEvent(body));
    }

}
