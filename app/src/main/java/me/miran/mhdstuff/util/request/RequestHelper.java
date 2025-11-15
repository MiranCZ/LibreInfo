package me.miran.mhdstuff.util.request;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.util.Log;

import me.miran.mhdstuff.exception.AppException;
import me.miran.mhdstuff.exception.RequestException;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.storage.StopMapper;
import me.miran.mhdstuff.util.IOUtil;
import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RequestHelper {


    public static long getLastStaticUpdate(Context context) throws AppException {
        try {
            InputStream stream = readUrl(context, Endpoint.STATIC_GTFS.resolve("info"));
            String output = new String(IOUtil.readAllBytes(stream), StandardCharsets.UTF_8);

            JsonObject json = new Gson().fromJson(output, JsonObject.class);

            if (json.has("lastUpdated")) {
                return json.get("lastUpdated").getAsLong();
            }
            return -1;
        } catch (IOException e) {
            throw new AppException("Failed to parse server info");
        }
    }

    public static InputStream getData(Context context) throws RequestException {
        return readUrl(context, Endpoint.STATIC_GTFS.resolve("data"));
    }


    // we are just fucked
    public static JsonArray getNews() {
        return new JsonArray(); // TODO
    }

    public static JsonArray getEvents(Context context) throws RequestException {
        return makeOwnRequest(context, "events", JsonArray.class);
    }

    public static JsonArray getDiversions(Context context) throws RequestException {
        return makeOwnRequest(context, "diversions", JsonArray.class);
    }

    public static JsonObject getRouteDelays(Context context) throws RequestException {
        return makeOwnRequest(context, "routedelays", JsonObject.class);
    }

    public static JsonArray getVehicles(Context context) throws RequestException {
        return makeOwnRequest(context, "vehicles", JsonArray.class);
    }

    public static JsonObject getVehicleInfo(Context context, int lineId, int routeId) throws RequestException {
        return makeOwnRequest(context, "vehicleinfo?lineid="+lineId+"&routeid="+routeId, JsonObject.class);
    }

    public static JsonObject getStopDelays(Context context, int stopId) throws RequestException {
        StopMapper mapper = IdStorage.getStopMapper();
        stopId = mapper.getOriginal(stopId);

        return makeOwnRequest(context, "stopdelays?stopid="+stopId, JsonObject.class);
    }

    private static <T extends JsonElement> T makeOwnRequest(Context context, String endpoint, Class<T> type) throws RequestException {
        try {
            InputStream stream = readUrl(context, Endpoint.APP_SERVER.resolve(endpoint));
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

    private static InputStream readUrl(Context context, Endpoint endpoint) throws RequestException {
        if (!hasNetwork(context)) {
            Log.d("RequestHelper", "assuming network is unreachable");
            throw RequestException.reachError(endpoint);
        }
        
        try {
            URL url = new URL(endpoint.url);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            return con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw RequestException.reachError(endpoint);
        }
    }
    
    private static boolean hasNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
    
        if (cm == null) return true; //not sure if its safe to assume wifi is not connected here

        Network network = cm.getActiveNetwork();
        if (network == null) return false; 
    
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
    
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED); 
    } 
    
}
