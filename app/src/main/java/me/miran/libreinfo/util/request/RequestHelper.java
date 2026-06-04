package me.miran.libreinfo.util.request;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import me.miran.libreinfo.exception.AppException;
import me.miran.libreinfo.exception.RequestException;
import me.miran.libreinfo.parsing.storage.IdStorage;
import me.miran.libreinfo.parsing.storage.StopMapper;
import me.miran.libreinfo.parsing.types.stop.StopId;
import me.miran.libreinfo.util.IOUtil;
import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
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


    public static JsonObject getNews(Context context) throws RequestException{
        return makeOwnRequest(context, "news", JsonObject.class);
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

    public static JsonObject getStopDelays(Context context, StopId stopId) throws RequestException {
        StopMapper mapper = IdStorage.getStopMapper();

        return makeOwnRequest(context, "stopdelays?stopid="+stopId.original(), JsonObject.class);
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

        // TODO make this into a setting
        final int readTimeoutMs = 10_000;
        try {
            URL url = new URL(endpoint.url);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(readTimeoutMs);

            return con.getInputStream();
        } catch (SocketTimeoutException e) {
            throw RequestException.timedOutError(endpoint, readTimeoutMs);
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
