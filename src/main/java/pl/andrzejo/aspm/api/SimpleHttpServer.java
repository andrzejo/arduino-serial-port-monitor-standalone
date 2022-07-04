package pl.andrzejo.aspm.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

public class SimpleHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleHttpServer.class);
    private HttpServer server;

    public SimpleHttpServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(4255), 0);
            server.setExecutor(null);
            server.start();
        } catch (Exception e) {
            logger.warn("Failed to start REST API server. API not available!", e);
        }
    }

    public void addEndpoint(String name, Function<String, String> handler) {
        if (server == null) {
            return;
        }

        server.createContext("/api/" + name, exchange -> {
            String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
            logger.info("Rest api request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI() + " [" + body + "]");
            if (!StringUtils.equalsIgnoreCase(exchange.getRequestMethod(), "POST")) {
                sendResponse(exchange, 405, "Invalid HTTP method " + exchange.getRequestMethod() + ". Use POST.");
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
