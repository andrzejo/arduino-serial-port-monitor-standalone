package pl.andrzejo.aspm.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.factory.BeanFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

public class SimpleHttpServer {
    public static final int PORT = 4255;

    public enum Method {
        Get, Post
    }

    private static final Logger logger = LoggerFactory.getLogger(SimpleHttpServer.class);
    private HttpServer server;

    public SimpleHttpServer() {
        try {
            server = BeanFactory.instance(HttpServer.class, this::createHttpServer);
            server.setExecutor(null);
            server.start();
        } catch (Exception e) {
            logger.warn("Failed to start application API server. API not available!", e);
        }
    }

    private HttpServer createHttpServer() {
        try {
            return HttpServer.create(new InetSocketAddress(PORT), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAddress() {
        return String.format("http://localhost:%s", PORT);
    }

    public void addEndpoint(Method method, String name, Function<String, String> handler) {
        if (server == null) {
            return;
        }
        String path = name == null ? "/" : name;
        server.createContext(path, exchange -> {
            String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
            logger.info("Rest api request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI() + " [" + body + "]");
            if (!StringUtils.equalsIgnoreCase(exchange.getRequestMethod(), method.name())) {
                String message = String.format("Invalid HTTP method '%s' For endpoint '%s'. Use '%s' instead.", exchange.getRequestMethod(), path, method.name().toUpperCase());
                sendResponse(exchange, 405, message);
                return;
            }
            try {
                String response = handler.apply(body);
                sendResponse(exchange, 200, response);
            } catch (Exception e) {
                logger.error("Request handler failed: " + exchange.getRequestURI(), e);
                sendResponse(exchange, 500, "Request failed");
            }
        });
    }

    private void sendResponse(HttpExchange exchange, int httpCode, String message) {
        try {
            String response = trimToEmpty(message);
            exchange.sendResponseHeaders(httpCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
