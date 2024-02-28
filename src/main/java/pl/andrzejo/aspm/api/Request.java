/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2024.
 */

package pl.andrzejo.aspm.api;

import java.net.URI;

public class Request {
    private final String body;
    private final SimpleHttpServer.Method method;
    private final URI requestURI;

    public Request(String body, SimpleHttpServer.Method method, URI requestURI) {
        this.body = body;
        this.method = method;
        this.requestURI = requestURI;
    }

    public String getBody() {
        return body;
    }

    public SimpleHttpServer.Method getMethod() {
        return method;
    }

    public URI getRequestURI() {
        return requestURI;
    }

}
