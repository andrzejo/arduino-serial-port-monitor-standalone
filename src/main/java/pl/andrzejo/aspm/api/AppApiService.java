/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.andrzejo.aspm.api.endpoints.*;
import pl.andrzejo.aspm.api.handler.AbstractApiHandler;
import pl.andrzejo.aspm.api.handler.ApiEndpoint;
import pl.andrzejo.aspm.api.handler.MethodInvoker;
import pl.andrzejo.aspm.api.server.SimpleHttpServer;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiExecuteCommand;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.*;
import static pl.andrzejo.aspm.api.server.SimpleHttpServer.Method.Post;
import static pl.andrzejo.aspm.api.server.SimpleHttpServer.methodRequirePathId;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class AppApiService {
    private final ApplicationEventBus eventBus;
    private final List<Endpoint> endpoints = new ArrayList<>();

    public AppApiService() {
        eventBus = instance(ApplicationEventBus.class);
    }

    public static String getRootEndpointAddress() {
        return SimpleHttpServer.getAddress();
    }

    public void start() {
        SimpleHttpServer server = instance(SimpleHttpServer.class);
        server.handleResources();
        run(server,
                new DeviceApi(),
                new WindowApi(),
                new MonitorApi(),
                new CmdApi(),
                new RootApi(endpoints)
        );
    }

    private void run(SimpleHttpServer server, AbstractApiHandler... apis) {
        for (AbstractApiHandler api : apis) {
            try {
                List<Endpoint> handlers = getEndpoints(api);
                handlers.forEach(h -> {
                    endpoints.add(h);
                    MethodInvoker invoker = new MethodInvoker(api, h.getHandlerMethod());
                    server.addEndpoint(h.getMethod(), h.getPath(), (request) -> {
                        if (h.getMethod() == Post) {
                            eventBus.post(new ApiExecuteCommand(h.getPath(), request.getBody()));
                        }
                        return invoker.invoke(request);
                    });
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<Endpoint> getEndpoints(AbstractApiHandler instance) {
        String basepath = handlerBasePath(instance);
        return Arrays.stream(instance.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(ApiEndpoint.class))
                .filter(m -> {
                    Class<?>[] types = m.getParameterTypes();
                    if (types.length == 0) {
                        return true;
                    }
                    if (types[0].equals(Request.class)) {
                        return true;
                    }
                    return types.length == 1;
                })
                .filter(m -> {
                    Class<?> returnType = m.getReturnType();
                    return returnType.equals(String.class) || returnType.equals(void.class);
                })
                .map(m -> {
                    ApiEndpoint annotation = m.getAnnotation(ApiEndpoint.class);
                    return Endpoint.fromAnnotation(basepath, m, annotation);
                })
                .sorted(Comparator.comparingInt(c -> c.getDescription().getOrder()))
                .collect(Collectors.toList());

    }

    private String handlerBasePath(AbstractApiHandler instance) {
        String basePath = instance.getBasePath();
        if (isNotBlank(basePath)) {
            return basePath;
        }
        return instance.getClass().getSimpleName().toLowerCase().replace("api", "");
    }

    private static String fullApiPath(String path, String path1) {
        path = strip(trimToEmpty(path), "/");
        path1 = strip(trimToEmpty(path1), "/");
        if (path.isEmpty() && path1.isEmpty()) {
            return "/";
        }
        return "/api/" + path + "/" + path1;
    }

    @Getter
    @RequiredArgsConstructor
    public static class EndpointDescription {
        private final String group;
        private final String desc;
        private final String bodyExample;
        private final String queryParams;
        private final int order;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Endpoint {
        private final Method handlerMethod;
        private final SimpleHttpServer.Method method;
        private final String path;
        private final String pathParam;
        private final EndpointDescription description;

        public String getDisplayPath() {
            if (methodRequirePathId(method)) {
                String p = isNotBlank(pathParam) ? pathParam : "id";
                return path + "/<" + p + ">";
            }
            return path;
        }

        public static Endpoint fromAnnotation(String basepath, Method handler, ApiEndpoint annotation) {
            String pathSuffix = isBlank(annotation.path()) ? handler.getName() : annotation.path();
            String path = fullApiPath(basepath, pathSuffix);
            EndpointDescription description = new EndpointDescription(basepath, annotation.description(),
                    annotation.bodyExample(), annotation.queryParams(), annotation.order());
            return new Endpoint(handler, annotation.method(), path, annotation.pathParam(), description);
        }
    }
}
