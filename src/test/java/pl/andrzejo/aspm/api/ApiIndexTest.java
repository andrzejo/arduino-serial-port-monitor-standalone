/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.api;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.andrzejo.aspm.api.server.SimpleHttpServer.Method.Post;

class ApiIndexTest {

    @SneakyThrows
    @Test
    void shouldGetApiIndex() {
        //given
        ApiIndex index = new ApiIndex();
        Method handler = getClass().getDeclaredMethod("handler");
        AppApiService.EndpointDescription desc = new AppApiService.EndpointDescription("test", "Some endpoint.", "BODY", "", 1);
        List<AppApiService.Endpoint> endpoints = Collections.singletonList(
                new AppApiService.Endpoint(handler, Post, "/api/endpoint", "", desc)
        );

        //when
        String html = index.getHtml(endpoints);

        //then
        assertThat(html)
                .contains("<div class=\"item\">")
                .contains("<div class=\"method post\"><span>POST</span></div>")
                .contains("<div class=\"path\">/api/endpoint</div>")
                .contains("<div class=\"desc\">Some endpoint.</div>")
                .contains("<div class=\"curl\"><span>curl -X POST http://localhost:4255/api/endpoint -d 'BODY' </span></div>");
    }

    private void handler() {

    }

}
