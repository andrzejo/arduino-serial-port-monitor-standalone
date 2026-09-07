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
import pl.andrzejo.aspm.gui.cmd.CommandItem;
import pl.andrzejo.aspm.gui.cmd.CommandsListApiBridge;
import pl.andrzejo.aspm.utils.MapUtil;

import static pl.andrzejo.aspm.api.server.SimpleHttpServer.Method.*;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;

@SuppressWarnings("unused")
public class CmdApi extends AbstractApiHandler {
    public static final String EXAMPLE_COMMAND_JSON = "{\"command\": \"AT+ECHO\", \"description\": \"Send echo\"}";
    private final CommandsListApiBridge commandApiBridge = instance(CommandsListApiBridge.class);

    @ApiEndpoint(
            method = SimpleHttpServer.Method.Post,
            description = "Send command to a connected device.",
            bodyExample = "ECHO TEST",
            order = 1
    )
    public void execute(Request request) {
        String body = request.getBody();
        eventBus.post(new ApiExecuteCommandEvent(body));
    }

    @ApiEndpoint(
            description = "List defined commands.",
            order = 2
    )
    public String list(Request request) {
        return serializer.serialize(commandApiBridge.getCommands());
    }

    @ApiEndpoint(
            method = Post,
            description = "Add a new command.",
            bodyExample = EXAMPLE_COMMAND_JSON,
            order = 3
    )
    public String add(Request request) {
        CommandItem cmd = serializer.deserialize(request.getBody(), CommandItem.class);
        String index = String.valueOf(commandApiBridge.add(cmd));
        return serializer.serialize(MapUtil.map("index", index));
    }

    @ApiEndpoint(
            method = Put,
            description = "Update command by index.",
            pathParam = "index",
            bodyExample = EXAMPLE_COMMAND_JSON,
            order = 4
    )
    public void update(Request request) {
        CommandItem cmd = serializer.deserialize(request.getBody(), CommandItem.class);
        int index = request.getPathId();
        commandApiBridge.update(index, cmd);
    }

    @ApiEndpoint(
            method = Delete,
            description = "Delete command by index.",
            pathParam = "index",
            order = 5
    )
    public void delete(Request request) {
        int index = request.getPathId();
        commandApiBridge.delete(index);
    }

}
