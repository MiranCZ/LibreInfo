package com.example.mhdstuff.util.request;

import com.example.mhdstuff.exception.RequestException;
import com.example.mhdstuff.util.IOUtil;
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


//    private static final String URL_START = "10.0.2.2/api";
//    private static final String STATIC_DATA_URL = "https://mirancz.github.io/gtfsstatic/";

    public static InputStream getApi() throws RequestException {
        return readStaticGtfs("api");
    }

    public static InputStream getCalendar() throws RequestException {
        return readStaticGtfs("calendar");
    }

    public static InputStream getCalendarDates() throws RequestException {
        return readStaticGtfs("calendar_dates");
    }

    public static InputStream getStops() throws RequestException {
        return readStaticGtfs("stops");
    }

    public static InputStream getStopTimes() throws RequestException {
        return readStaticGtfs("stop_times");
    }

    public static InputStream getTrips() throws RequestException {
        return readStaticGtfs("trips");
    }

    public static InputStream getLineAliases() throws RequestException {
        return readStaticGtfs("lines");
    }
    
    public static InputStream getRouteStops() throws RequestException {
        return readStaticGtfs("route_stops");
    }

    public static InputStream getPosts() throws RequestException {
        return readStaticGtfs("posts");
    }

    private static InputStream readStaticGtfs(String endpoint) throws RequestException {
        return readUrl(Endpoint.STATIC_GTFS.resolve("parsed", endpoint));
    }

    // we are just fucked
    public static JsonArray getNews() {
        return new JsonArray(); // TODO
    }

    public static JsonArray getEvents() throws RequestException {
        return makeOwnRequest("events", JsonArray.class);
    }

    public static JsonArray getDiversions() throws RequestException {
        return makeOwnRequest("diversions", JsonArray.class);
    }

    public static JsonObject getRouteDelays() throws RequestException {
        return makeOwnRequest("routedelays", JsonObject.class);
    }

    public static JsonArray getVehicles() throws RequestException {
        return makeOwnRequest("vehicles", JsonArray.class);
    }

    public static JsonObject getVehicleInfo(int lineId, int routeId) throws RequestException {
        return makeOwnRequest("vehicleinfo?lineid="+lineId+"&routeid="+routeId, JsonObject.class);
    }

    public static JsonObject getStopDelays(int stopId) throws RequestException {
        return makeOwnRequest("stopdelays?stopid="+stopId, JsonObject.class);
    }

    private static <T extends JsonElement> T makeOwnRequest(String endpoint, Class<T> type) throws RequestException {
        try {
            InputStream stream = readUrl(Endpoint.APP_SERVER.resolve(endpoint));
            if (stream == null) {
                throw RequestException.reachError(Endpoint.APP_SERVER);
            }

            String output = new String(IOUtil.readAllBytes(stream), StandardCharsets.UTF_8);
            stream.close();

            if (output.isBlank()) {
                throw RequestException.readError(Endpoint.APP_SERVER);
            }

            return new Gson().fromJson(output, type);
        } catch (RequestException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RequestException("IO Exception "+e.getMessage(), Endpoint.APP_SERVER);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            throw RequestException.parseError(Endpoint.APP_SERVER);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RequestException("Unexpected error ",Endpoint.APP_SERVER);
        }
    }

    private static InputStream readUrl(Endpoint endpoint) throws RequestException {
        try {
            URL url = new URL(endpoint.url);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            return con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw RequestException.reachError(endpoint);
        }
    }
}
