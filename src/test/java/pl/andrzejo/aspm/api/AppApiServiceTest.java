/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.api;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.BusEvent;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiCloseDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiExecuteCommand;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiOpenDeviceEvent;
import pl.andrzejo.aspm.factory.BeanFactory;
import pl.andrzejo.aspm.serial.SerialPorts;
import pl.andrzejo.aspm.service.SerialHandlerService;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Get;
import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Post;

class AppApiServiceTest {
    private ApplicationEventBus bus;
    private SimpleHttpServerTesting serverTesting;
    private AppApiService service;
    private ApiIndex apiIndex;

    @BeforeEach
    void setUp() {
        BeanFactory.reset();
        BeanFactory.overrideInstance(HttpServer.class, mock(HttpServer.class));
        apiIndex = mock(ApiIndex.class);

        bus = mock(ApplicationEventBus.class);
        serverTesting = new SimpleHttpServerTesting();

        BeanFactory.overrideInstance(ApiIndex.class, apiIndex);
        BeanFactory.overrideInstance(SimpleHttpServer.class, serverTesting);
        BeanFactory.overrideInstance(ApplicationEventBus.class, bus);
        service = new AppApiService();
    }

    @Test
    void shouldAddAllEndpoints() {
        //given
        AppApiService service = new AppApiService();
        SimpleHttpServer server = mock(SimpleHttpServer.class);
        BeanFactory.overrideInstance(SimpleHttpServer.class, server);

        //when
        service.start();

        //then
        verify(server).addEndpoint(eq(Post), eq("/api/device/open"), anyObject());
        verify(server).addEndpoint(eq(Post), eq("/api/device/close"), anyObject());
        verify(server).addEndpoint(eq(Get), eq("/api/device/status"), anyObject());
        verify(server).addEndpoint(eq(Get), eq("/api/device/list"), anyObject());
    }

    @Test
    void shouldTestOpenEndpoint() throws URISyntaxException {
        //given
        //when
        service.start();

        //then
        MethodDefinition definition = getMethodDefinition(Post, "/api/device/open");

        String response = invokeHandler(definition, new Request("COM1", Post, new URI("")));
        assertThat(response).isNull();

        List<BusEvent> events = verifyMethodPostEventBusEvents(bus);

        ApiExecuteCommand event1 = verifyEventIsInstanceOf(events.get(0), ApiExecuteCommand.class);
        assertThat(event1.getBody()).isEqualTo("COM1");
        assertThat(event1.getCommand()).isEqualTo("/api/device/open");

        ApiOpenDeviceEvent event2 = verifyEventIsInstanceOf(events.get(1), ApiOpenDeviceEvent.class);
        assertThat(event2.getDevice()).isEqualTo("COM1");
    }

    private MethodDefinition getMethodDefinition(SimpleHttpServer.Method method, String endpoint) {
        Map<String, MethodDefinition> methods = serverTesting.getMethods();
        MethodDefinition definition = methods.get(endpoint);
        assertNotNull(definition, "There is no endpoint " + endpoint);
        assertEquals(method, definition.getMethod(), "Invalid HTTP method for endpoint " + endpoint);
        return definition;
    }

    @Test
    void shouldTestCloseEndpoint() {
        //given
        //when
        service.start();

        //then
        MethodDefinition definition = getMethodDefinition(Post, "/api/device/close");

        String response = invokeHandler(definition, new Request(null, Post, null));
        assertThat(response).isNull();

        List<BusEvent> events = verifyMethodPostEventBusEvents(bus);

        ApiExecuteCommand event1 = verifyEventIsInstanceOf(events.get(0), ApiExecuteCommand.class);
        assertThat(event1.getBody()).isNull();
        assertThat(event1.getCommand()).isEqualTo("/api/device/close");

        verifyEventIsInstanceOf(events.get(1), ApiCloseDeviceEvent.class);
    }

    @Test
    void shouldTestStatusEndpoint() {
        //given
        SerialHandlerService serialService = mock(SerialHandlerService.class);
        BeanFactory.overrideInstance(SerialHandlerService.class, serialService);
        when(serialService.getStatus()).thenReturn(new SerialHandlerService.Status(new DeviceConfig(), false));

        //when
        service.start();

        //then
        MethodDefinition definition = getMethodDefinition(Get, "/api/device/status");

        String response = invokeHandler(definition, null);
        assertThat(response).isEqualTo("DeviceOpen: false\n");
    }

    @Test
    void shouldTestStatusEndpoint2() {
        //given
        SerialHandlerService serialService = mock(SerialHandlerService.class);
        BeanFactory.overrideInstance(SerialHandlerService.class, serialService);
        DeviceConfig config = new DeviceConfig();
        config.setDevice("COM1").setBaud(192000).setParity('N');
        when(serialService.getStatus()).thenReturn(new SerialHandlerService.Status(config, true));

        //when
        service.start();

        //then
        MethodDefinition definition = getMethodDefinition(Get, "/api/device/status");

        String response = invokeHandler(definition, null);
        assertThat(response).isEqualTo("DeviceOpen: true\n" +
                "Device: COM1\n" +
                "Baud: 192000\n" +
                "Parity: N\n" +
                "DataBits: 0\n" +
                "StopBits: 0.0\n" +
                "DTR: false\n" +
                "RTS: false\n");
    }

    @Test
    void shouldTestDevicesEndpoint() {
        //given
        SerialPorts ports = mock(SerialPorts.class);
        when(ports.getList()).thenReturn(Arrays.asList("COM1", "COM3", "COM4"));
        BeanFactory.overrideInstance(SerialPorts.class, ports);

        //when
        service.start();

        //then
        MethodDefinition definition = getMethodDefinition(Get, "/api/device/list");

        String response = invokeHandler(definition, null);
        assertThat(response).isEqualTo("COM1\nCOM3\nCOM4");
    }

    @Test
    void shouldTestRootEndpoint() {
        //given
        when(apiIndex.getHtml(any())).thenReturn("API INDEX");
        //when
        service.start();

        //then
        MethodDefinition definition = getMethodDefinition(Get, null);

        String response = invokeHandler(definition, null);
        assertThat(response).isEqualTo("API INDEX");

        @SuppressWarnings("unchecked,rawtypes")
        Class<List<AppApiService.Endpoint>> listClass = (Class<List<AppApiService.Endpoint>>) (Class) List.class;

        ArgumentCaptor<List<AppApiService.Endpoint>> captor = ArgumentCaptor.forClass(listClass);
        verify(apiIndex).getHtml(captor.capture());
        List<AppApiService.Endpoint> list = captor.getValue();
        assertThat(list).hasSize(8);
    }

    @Test
    void shouldGetRootEndpointAddress() {
        //given
        //when
        String address = AppApiService.getRootEndpointAddress();

        //then
        assertEquals("http://localhost:4255", address);
    }

    @SuppressWarnings("unchecked")
    private <T extends BusEvent> T verifyEventIsInstanceOf(BusEvent busEvent, Class<T> eventClass) {
        assertThat(busEvent).isInstanceOf(eventClass);
        return (T) busEvent;
    }

    private List<BusEvent> verifyMethodPostEventBusEvents(ApplicationEventBus bus) {
        ArgumentCaptor<BusEvent> captor = ArgumentCaptor.forClass(BusEvent.class);
        verify(bus, times(2)).post(captor.capture());
        return captor.getAllValues();
    }

    private String invokeHandler(MethodDefinition definition, Request request) {
        return definition.getHandler().apply(request);
    }

    static class SimpleHttpServerTesting extends SimpleHttpServer {
        private final Map<String, MethodDefinition> methods = new HashMap<>();

        @Override
        public void addEndpoint(Method method, String name, Function<Request, String> handler) {
            methods.put(name, new MethodDefinition(method, name, handler));
        }

        public Map<String, MethodDefinition> getMethods() {
            return methods;
        }
    }

    static class MethodDefinition {
        private final SimpleHttpServer.Method method;
        private final String name;
        private final Function<Request, String> handler;

        MethodDefinition(SimpleHttpServer.Method method, String name, Function<Request, String> handler) {
            this.method = method;
            this.name = name;
            this.handler = handler;
        }

        public SimpleHttpServer.Method getMethod() {
            return method;
        }

        public String getName() {
            return name;
        }

        public Function<Request, String> getHandler() {
            return handler;
        }
    }
}
