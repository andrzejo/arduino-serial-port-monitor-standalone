package pl.andrzejo.aspm.api;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.andrzejo.aspm.factory.BeanFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Get;
import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Post;

class SimpleHttpServerTest {

    @BeforeEach
    void setUp() {
        BeanFactory.reset();
    }

    @Test
    void shouldHandleHttpRequest() throws IOException {
        //given
        HttpServerTesting serverTesting = new HttpServerTesting();
        BeanFactory.overrideInstance(HttpServer.class, serverTesting);
        SimpleHttpServer server = new SimpleHttpServer();

        //when
        server.addEndpoint(Post, "/api/test", (i) -> "response");

        //then
        HttpHandler handler = serverTesting.contexts.get("/api/test");
        HttpExchange exchange = getExchange();
        handler.handle(exchange);
        verify(exchange).sendResponseHeaders(eq(200), eq(8L));
        assertEquals(exchange.getResponseBody().toString(), "response");
    }

    @Test
    void shouldHandleExceptionInHttpRequest() throws IOException {
        //given
        HttpServerTesting serverTesting = new HttpServerTesting();
        BeanFactory.overrideInstance(HttpServer.class, serverTesting);
        SimpleHttpServer server = new SimpleHttpServer();

        //when
        server.addEndpoint(Post, "/api/test", (i) -> {
            throw new RuntimeException("Error in handler");
        });

        //then
        HttpHandler handler = serverTesting.contexts.get("/api/test");
        HttpExchange exchange = getExchange();
        handler.handle(exchange);
        verify(exchange).sendResponseHeaders(eq(500), eq(14L));
        assertEquals(exchange.getResponseBody().toString(), "Request failed");
    }

    @Test
    void shouldHandleRequestInvalidMethod() throws IOException {
        //given
        HttpServerTesting serverTesting = new HttpServerTesting();
        BeanFactory.overrideInstance(HttpServer.class, serverTesting);
        SimpleHttpServer server = new SimpleHttpServer();

        //when
        server.addEndpoint(Get, "/api/test", (i) -> "test response");

        //then
        HttpHandler handler = serverTesting.contexts.get("/api/test");
        HttpExchange exchange = getExchange();
        handler.handle(exchange);
        verify(exchange).sendResponseHeaders(eq(405), eq(71L));
        assertEquals(exchange.getResponseBody().toString(), "Invalid HTTP method 'Post' For endpoint '/api/test'. Use 'GET' instead.");
    }

    private HttpExchange getExchange() {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(new byte[]{}));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(output);
        when(exchange.getRequestMethod()).thenReturn(Post.name());
        return exchange;
    }

    static class HttpServerTesting extends HttpServer {
        protected Map<String, HttpHandler> contexts = new HashMap<>();

        @Override
        public void bind(InetSocketAddress addr, int backlog) throws IOException {

        }

        @Override
        public void start() {

        }

        @Override
        public void setExecutor(Executor executor) {

        }

        @Override
        public Executor getExecutor() {
            return null;
        }

        @Override
        public void stop(int delay) {

        }

        @Override
        public HttpContext createContext(String path, HttpHandler handler) {
            contexts.put(path, handler);
            return null;
        }

        @Override
        public HttpContext createContext(String path) {
            return null;
        }

        @Override
        public void removeContext(String path) throws IllegalArgumentException {

        }

        @Override
        public void removeContext(HttpContext context) {

        }

        @Override
        public InetSocketAddress getAddress() {
            return null;
        }
    }
}