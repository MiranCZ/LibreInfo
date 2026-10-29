package me.miran.request;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.miran.ids.*;
import me.miran.util.IdStorage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.miran.request.Util.decodeGooglePolyline;

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



    public static VehiclesResponse getVehicles() {
        VehiclesResponse vehiclesResponse = new Gson().fromJson(get("vehicles"), VehiclesResponse.class);

        return new VehiclesResponse(vehiclesResponse.vehicles().stream().map(ApiVehicle::mapIds).filter(Objects::nonNull).toList());
    }

    public static String getDiversions() {
        return get("diversions");
    }

    public static String getEvents() {
        return get("events");
    }

    public static PostsResponse getPosts() {
        // not mapped: the only consumer is gtfs-parser, which maps stops itself once it has written
        // the new id tables (ours might not know the new stops yet)
        return new Gson().fromJson(get("posts"), PostsResponse.class);
    }

    public static List<Pos> getRoutePath(String serviceId, String lineId, String routeId) {
        var res =  get("routePath", Map.of(
                "serviceid", serviceId,
                "lineid", lineId,
                "routeid", routeId
        ));


        var response = new Gson().fromJson(res, RoutePathResponse.class);

        ArrayList<Pos> list = new ArrayList<>();
        for (RoutePart stop : response.getStops()) {
            list.addAll(decodeGooglePolyline(stop.getPath()));
        }
        return list;


    }

    public static String getDepartures(int stopId) {
        Integer upstreamStopId = IdStorage.STOP.getOriginal(stopId);
        if (upstreamStopId == null) {
            throw new IllegalArgumentException("Unknown stop " + stopId);
        }

        String body = get("departures", Map.of("stopid", upstreamStopId.toString()));

        // the response echoes the stop, lines and routes are not mapped (only stops and services are)
        JsonElement parsed = JsonParser.parseString(body);
        if (parsed.isJsonObject() && parsed.getAsJsonObject().has("stopId")) {
            parsed.getAsJsonObject().addProperty("stopId", stopId);
        }

        return parsed.toString();
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
