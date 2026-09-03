/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.api.endpoints;

import pl.andrzejo.aspm.api.ApiIndex;
import pl.andrzejo.aspm.api.AppApiService;
import pl.andrzejo.aspm.api.handler.AbstractApiHandler;
import pl.andrzejo.aspm.api.handler.ApiEndpoint;
import pl.andrzejo.aspm.api.server.SimpleHttpServer;

import java.util.List;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

@SuppressWarnings("unused")
public class RootApi extends AbstractApiHandler {
    private final ApiIndex apiIndex;
    private final List<AppApiService.Endpoint> endpoints;

    public RootApi(List<AppApiService.Endpoint> endpoints) {
        this.endpoints = endpoints;
        this.apiIndex = instance(ApiIndex.class);
    }

    @ApiEndpoint(
            method = SimpleHttpServer.Method.Get,
            path = "/",
            description = "Get endpoints."
    )
    public String endpoints() {
        return apiIndex.getHtml(endpoints);
    }

    public String getBasePath() {
        return "/";
    }

}
