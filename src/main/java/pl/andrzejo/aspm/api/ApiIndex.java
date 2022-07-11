/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.api;

import pl.andrzejo.aspm.App;
import pl.andrzejo.aspm.utils.AppFiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang.text.StrSubstitutor.replace;

public class ApiIndex {
    private static final Map<SimpleHttpServer.Method, String> templates = new HashMap<>();

    static {
        templates.put(SimpleHttpServer.Method.Get, AppFiles.readResources("api/method.get.html"));
        templates.put(SimpleHttpServer.Method.Post, AppFiles.readResources("api/method.post.html"));
    }

    public String getHtml(List<AppApiService.Endpoint> endpoints) {
        String html = AppFiles.readResources("api/index.html");
        String endpointsHtml = getEndpointsHtml(endpoints);
        Map<String, String> map = new HashMap<>();
        map.put("APP", App.Name);
        map.put("ENDPOINTS", endpointsHtml);
        return replace(html, map);
    }

    private String getEndpointsHtml(List<AppApiService.Endpoint> endpoints) {
        return endpoints.stream().map(this::getEndpointHtml).collect(Collectors.joining());
    }

    private String getEndpointHtml(AppApiService.Endpoint endpoint) {
        String html = templates.get(endpoint.getMethod());
        Map<String, String> replacements = getReplacements(endpoint);
        return replace(html, replacements);
    }

    private Map<String, String> getReplacements(AppApiService.Endpoint endpoint) {
        HashMap<String, String> map = new HashMap<>();
        String suffix = endpoint.getPath() == null ? "/" : endpoint.getPath();
        String href = SimpleHttpServer.getAddress() + suffix;
        map.put("HREF", href);
        map.put("PATH", suffix);
        map.put("DESC", endpoint.getDesc());
        String curl = "curl -X " + endpoint.getMethod().name().toUpperCase() + " " + href;
        if (endpoint.getMethod() != SimpleHttpServer.Method.Get) {
            curl += " -d 'BODY' ";
        }
        map.put("CMD", curl);
        return map;
    }


}
