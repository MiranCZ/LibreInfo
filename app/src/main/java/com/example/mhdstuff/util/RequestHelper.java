package com.example.mhdstuff.util;

import com.google.gson.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class RequestHelper {


    private static final String URL_START = "10.0.2.2/api";


    public static JsonArray getEvents() {
        Optional<JsonObject> result = makeRequest("events", JsonObject.class);
        if (result.isEmpty()) return new JsonArray();

        return result.get().get("Events").getAsJsonArray();
    }

    public static JsonArray getStops() {
        Optional<JsonArray> result = makeRequest("stops", JsonArray.class);

        return result.orElse(new JsonArray());
    }

    public static JsonArray getPosts() {
        Optional<JsonArray> result = makeRequest("posts", JsonArray.class);

        return result.orElse(new JsonArray());
    }

    public static JsonArray getDiversions() {
        Optional<JsonObject> result = makeRequest("diversions", JsonObject.class);
        if (result.isEmpty()) return new JsonArray();

        return result.get().get("Diversions").getAsJsonArray();
    }

    public static JsonArray getNews() {
        Optional<JsonObject> result = makeRequest("news", JsonObject.class);
        if (result.isEmpty()) return new JsonArray();

        return result.get().get("News").getAsJsonArray();
    }

    public static JsonArray getVehiclesList() {
        JsonObject vehicles = getVehicles();
        if (!vehicles.has("Data")) return new JsonArray();

        return vehicles.get("Data").getAsJsonArray();
    }

    public static JsonObject getVehicles() {
        Optional<JsonObject> result = makeRequest("vehicles.json", JsonObject.class);

        return result.orElseGet(JsonObject::new);
    }
    public static JsonArray getLineAliases() {
        Optional<JsonObject> result = makeRequest("linealiases", JsonObject.class);
        if (result.isEmpty()) return new JsonArray();

        return result.get().get("LineAliases").getAsJsonArray();
    }

    public static JsonObject getDepartures(int stopID) {
        return getDepartures(stopID, -1);
    }

    public static JsonObject getDepartures(int stopID, int postID) {
        String request = "departures?StopID="+stopID;
        if (postID != -1) {
            request = request + "&PostID="+postID;
        }

        Optional<JsonObject> result = makeRequest(request, JsonObject.class);

        return result.orElseGet(JsonObject::new);

    }

    private static <T extends JsonElement> Optional<T>  makeRequest(String endpoint, Class<T> type) {
        try {
            URL url = new URL(URL_START + endpoint);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            InputStream stream = con.getInputStream();


            String output = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            stream.close();

            return Optional.ofNullable(new Gson().fromJson(output, type));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
