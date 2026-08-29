/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2024.
 */

package pl.andrzejo.aspm.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URI;

@Getter
@RequiredArgsConstructor
public class Request {
    private final String body;
    private final SimpleHttpServer.Method method;
    private final URI requestURI;
}
