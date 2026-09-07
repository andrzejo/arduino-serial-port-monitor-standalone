/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.api;

import org.apache.commons.lang.StringEscapeUtils;
import pl.andrzejo.aspm.App;
import pl.andrzejo.aspm.api.server.SimpleHttpServer;
import pl.andrzejo.aspm.utils.AppFiles;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.text.StrSubstitutor.replace;
import static pl.andrzejo.aspm.utils.MapUtil.map;

public class ApiIndex {
    private static final Map<SimpleHttpServer.Method, String> templates = new HashMap<>();

    static {
        templates.put(SimpleHttpServer.Method.Get, AppFiles.readResources("html/api/method.get.html"));
        templates.put(SimpleHttpServer.Method.Post, AppFiles.readResources("html/api/method.post.html"));
        templates.put(SimpleHttpServer.Method.Put, AppFiles.readResources("html/api/method.put.html"));
        templates.put(SimpleHttpServer.Method.Delete, AppFiles.readResources("html/api/method.delete.html"));
    }

    public String getHtml(List<AppApiService.Endpoint> endpoints) {
        String html = AppFiles.readResources("html/api/index.html");
        String endpointsHtml = getEndpointsHtml(endpoints);
        Map<String, String> map = new HashMap<>();
        map.put("APP", App.Name);
        map.put("VERSION", App.Version.getVer());
        map.put("VERSION_DATE", App.Version.getDate());
        map.put("BUILD_YEAR", App.Version.getYear());
        map.put("URL", App.GitHubUrl);
        map.put("ENDPOINTS", endpointsHtml);
        return replace(html, map);
    }

    private String getEndpointsHtml(List<AppApiService.Endpoint> endpoints) {
        final String groupHtml = AppFiles.readResources("html/api/group.html");
        Map<String, List<AppApiService.Endpoint>> grouped = groupEndpoints(endpoints);

        StringBuilder allHtml = new StringBuilder();

        grouped.forEach((group, methods) -> {
            String epHtml = methods.stream().map(this::getEndpointHtml).collect(Collectors.joining());
            String html = replace(groupHtml, map("TITLE", group, "ENDPOINTS", epHtml));
            allHtml.append(html).append("\n");
        });

        return allHtml.toString();
    }

    private static Map<String, List<AppApiService.Endpoint>> groupEndpoints(List<AppApiService.Endpoint> endpoints) {
        Map<String, List<AppApiService.Endpoint>> grouped = new LinkedHashMap<>();
        for (AppApiService.Endpoint endpoint : endpoints) {
            String group = endpoint.getDescription().getGroup();
            String displayGroup = group.equals("/") ? "ROOT" : group;
            grouped.computeIfAbsent(displayGroup, k -> new ArrayList<>()).add(endpoint);
        }
        return grouped;
    }

    private String getEndpointHtml(AppApiService.Endpoint endpoint) {
        String html = templates.get(endpoint.getMethod());
        Objects.requireNonNull(html, "No template found for method: " + endpoint.getMethod());
        Map<String, String> replacements = getReplacements(endpoint);
        return replace(html, replacements);
    }

    private Map<String, String> getReplacements(AppApiService.Endpoint endpoint) {
        HashMap<String, String> map = new HashMap<>();
        String suffix = endpoint.getPath() == null ? "/" : endpoint.getDisplayPath();
        String href = SimpleHttpServer.getAddress() + suffix;
        map.put("HREF", href);
        map.put("PATH", escape(suffix));
        AppApiService.EndpointDescription description = endpoint.getDescription();
        map.put("DESC", escape(trimToEmpty(description.getDesc())));
        map.put("QUERY", escape(trimToEmpty(description.getQueryParams())));
        String curl = "curl -X " + endpoint.getMethod().name().toUpperCase() + " " + href;
        if (endpoint.getMethod() != SimpleHttpServer.Method.Get && isNotBlank(description.getBodyExample())) {
            curl += String.format(" -d '%s' ", description.getBodyExample());
        }
        map.put("CMD", escape(curl));
        return map;
    }

    private String escape(String html) {
        return StringEscapeUtils.escapeHtml(html);
    }


}
