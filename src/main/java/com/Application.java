package com;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class Application {

    public static void main(String[] args) throws IOException, URISyntaxException {

        Telegram.init();

        HttpServer server = HttpServer.create(new InetSocketAddress( 8002), 0);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        server.createContext("/", new HttpApiHandler());
        server.setExecutor(threadPoolExecutor);
        server.start();
        RssParser.start();
        System.out.println("24h server started on port 8002");
    }

    private static class HttpApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().getPath();
            //System.out.println(uri);
            if ("/".equals(uri)) {
                sendResource(exchange, "/index.html", "text/html");
            } else if ("/get_items".equals(uri)) {
                sendResponse(exchange, RssParser.getItemsJSON());
            } else if (uri.startsWith("/icons")) {
                sendResource(exchange, uri, "image/png");
            } else if ("/logo.svg".equals(uri)) {
                sendResource(exchange, uri, "image/svg+xml");
            }
        }

        private void sendResource(HttpExchange exchange, String resource, String contentType) throws IOException{
            InputStream inputStream = getClass().getResourceAsStream(resource);
            OutputStream out = exchange.getResponseBody();
            byte[] content;
            if ("image/png".equals(contentType)) {
                content = new byte[inputStream.available()];
                inputStream.read(content);
                Headers headers = exchange.getResponseHeaders();
                headers.add("Content-Type", contentType);
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String contents = reader.lines()
                        .collect(Collectors.joining(System.lineSeparator()));
                Headers headers = exchange.getResponseHeaders();
                headers.add("Content-Type", contentType);
                content = contents.getBytes(StandardCharsets.UTF_8);
            }
            exchange.sendResponseHeaders(200, content.length);
            out.write(content);
            out.close();
        }

        private void sendResponse(HttpExchange exchange, String response) throws IOException {
            OutputStream outputStream = exchange.getResponseBody();
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseHeaders().set("Content-type", "application/json");
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        }
    }

}
