package me.miran.request;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestHelper {

    private static final String SERVER_URL;
    private static final String TOKEN;
    private static final HttpClient HTTP;

    static {
        SERVER_URL = Util.readSecret("kordis_url");
        TOKEN = Util.readSecret("kordis_token");
        HTTP = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private RequestHelper() {
    }



    public static String getVehicles() {
        return get("vehicles");
    }

    public static String getDiversions() {
        return get("diversions");
    }

    public static String getEvents() {
        return get("events");
    }

    public static String getPosts() {
        return get("posts");
    }

    public static String getDepartures(String stopId) {
        return get("departures", Map.of("stopid", stopId));
    }


    private static String get(String path) {
        return get(path, Map.of());
    }

    private static String get(String path, Map<String, String> query) {
        URI uri = URI.create(SERVER_URL + path + encodeQuery(query));
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("X-Access-Token", TOKEN)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IllegalStateException("API returned HTTP " + response.statusCode() + " for " + path);
            }
            return response.body();
        } catch (IOException e) {
            throw new UncheckedIOException("Request to " + uri + " failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Request to " + uri + " was interrupted", e);
        }
    }

    private static String encodeQuery(Map<String, String> query) {
        if (query.isEmpty()) {
            return "";
        }
        return query.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&", "?", ""));
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }


}
