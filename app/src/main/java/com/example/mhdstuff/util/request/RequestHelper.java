package com.example.mhdstuff.util.request;

import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.zip.ZipInputStream;

public class RequestHelper {


    private static final String URL_START = "10.0.2.2/api";


    public static JsonArray getEvents() {
        Optional<JsonObject> result = makeRequest("events", JsonObject.class);
        if (result.isEmpty()) return new JsonArray();

        return result.get().get("Events").getAsJsonArray();
    }

    // this should be fine
    public static JsonArray getStops() {
        Optional<JsonArray> result = makeRequest("stops", JsonArray.class);

        return result.orElse(new JsonArray());
    }

    // also should be fine
    public static JsonArray getPosts() {
        Optional<JsonArray> result = makeRequest("posts", JsonArray.class);

        return result.orElse(new JsonArray());
    }

    // maybe? from and to fields seems to be missing
    public static JsonArray getDiversions() {
        Optional<JsonObject> result = makeRequest("diversions", JsonObject.class);
        if (result.isEmpty()) return new JsonArray();

        return result.get().get("Diversions").getAsJsonArray();
    }

    // we are just fucked
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

    // should be fine, can actually use the webhook
    public static JsonObject getVehicles() {
        Optional<JsonObject> result = makeRequest("vehicles.json", JsonObject.class);

        return result.orElseGet(JsonObject::new);
    }

    // idk about this one.. ughh
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

    public static ZipInputStream getStaticGTFS() {
        InputStream stream = readUrl("https://kordis-jmk.cz/gtfs/gtfs.zip");
        if (stream == null) return null;

        return new ZipInputStream(stream);
    }

    private static <T extends JsonElement> Optional<T>  makeRequest(String endpoint, Class<T> type) {
        try {
            InputStream stream = readUrl(URL_START+endpoint);
            if (stream == null) return Optional.empty();

            String output = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            stream.close();

            return Optional.ofNullable(new Gson().fromJson(output, type));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static InputStream readUrl(String stringUrl) {
        try {
            URL url = new URL(stringUrl);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            return con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
