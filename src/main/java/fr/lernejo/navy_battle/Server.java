package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

public class Server {
    public final String id;
    public final String url;
    public final String msg;

    public Server(String id, String url, String msg) {
        this.id = id;
        this.url = url;
        this.msg = msg;
    }

    public void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.createContext("/ping", this::pingHandler);
        server.createContext("/api/game/start", this::startGameHandler);
        server.start();
    }

    private void pingHandler(HttpExchange exchange) throws IOException {
        String body = "OK";
        exchange.sendResponseHeaders(200, body.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        }
    }

    private void startGameHandler(HttpExchange exchange) throws IOException {
        InputStream in = exchange.getRequestBody();
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(in, "UTF-8"));
            if (jsonObject.get("id") == null || jsonObject.get("url") == null || jsonObject.get("message") == null) {
                exchange.sendResponseHeaders(400, 0);
            }
        } catch (ParseException e) {
            exchange.sendResponseHeaders(400, 0);
            e.printStackTrace();
        }
        sendJson(exchange);
    }

    private void sendJson(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-type", "application/json");
        String body = "{\"id\":\""+ this.id + "\", \"url\":\"" + this.url + "\", \"message\":\""+ this.msg + "\"}";
        exchange.sendResponseHeaders(202, body.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        }
        exchange.close();
    }

    public void sendPostRequest(String adversaryUrl) throws IOException, InterruptedException {
        java.net.http.HttpClient client = HttpClient.newHttpClient();

        HttpRequest postRequest = HttpRequest.newBuilder()
            .uri(URI.create(adversaryUrl + "/api/game/start"))
            .setHeader("Accept", "application/json")
            .setHeader("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"id\":\"" + this.id + "\", \"url\":\"" + this.url + "\", \"message\":\"" + this.msg + "\"}"))
            .build();
        var response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
    }
}
