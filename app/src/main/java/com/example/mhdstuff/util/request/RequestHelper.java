package com.example.mhdstuff.util.request;

import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.zip.ZipInputStream;

public class RequestHelper {


    private static final String URL_START = "10.0.2.2/api";


    public static InputStream getStops() {
        return readUrl("https://mirancz.github.io/gtfsstatic/parsed/stops");
    }

    public static InputStream getStopTimes() {
        return readUrl("https://mirancz.github.io/gtfsstatic/parsed/stop_times");
    }

    public static InputStream getTrips() {
        return readUrl("https://mirancz.github.io/gtfsstatic/parsed/trips");
    }

    public static InputStream getLineAliases() {
        return readUrl("https://mirancz.github.io/gtfsstatic/parsed/routes");
    }

    // also should be fine - spoiler alert: It is not.
    public static JsonArray getPosts() {
        Optional<JsonArray> result = makeRequest("posts", JsonArray.class);

        return result.orElse(new JsonArray());
    }


    // we are just fucked
    public static JsonArray getNews() {
        if (true) return new JsonArray();

        Optional<JsonObject> result = makeRequest("news", JsonObject.class);
        if (result.isEmpty()) return new JsonArray();

        return result.get().get("News").getAsJsonArray();
    }

    public static InputStream getRouteStops() {
        return readUrl("https://mirancz.github.io/gtfsstatic/parsed/route_stops");
    }

    public static InputStream getCalendar() {
        return readUrl("https://mirancz.github.io/gtfsstatic/unzipped/calendar");
    }

    public static InputStream getCalendarDates() {
        return readUrl("https://mirancz.github.io/gtfsstatic/unzipped/calendar_dates");
    }

    public static ZipInputStream getStaticGTFS() {
        InputStream stream = readUrl("https://kordis-jmk.cz/gtfs/gtfs.zip");
        if (stream == null) return null;

        return new ZipInputStream(stream);
    }

    private static <T extends JsonElement> Optional<T> makeRequest(String endpoint, Class<T> type) {
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
