package me.miran.libreinfo.util.request;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import me.miran.libreinfo.R;
import me.miran.libreinfo.exception.AppException;
import me.miran.libreinfo.exception.RequestException;
import me.miran.libreinfo.parsing.storage.IdStorage;
import me.miran.libreinfo.parsing.storage.StopMapper;
import me.miran.libreinfo.parsing.types.stop.Stop;
import me.miran.libreinfo.parsing.types.stop.StopId;
import me.miran.libreinfo.util.AppLog;
import me.miran.libreinfo.util.IOUtil;
import me.miran.libreinfo.util.Text;
import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RequestHelper {


    public static long getLastStaticUpdate(Context context) throws AppException {
        try (InputStream stream = readUrl(context, Endpoint.STATIC_GTFS.resolve("info"))) {
            String output = new String(IOUtil.readAllBytes(stream), StandardCharsets.UTF_8);

            JsonObject json = new Gson().fromJson(output, JsonObject.class);

            if (json.has("lastUpdated")) {
                return json.get("lastUpdated").getAsLong();
            }
            return -1;
        } catch (IOException e) {
            throw new AppException(Text.translatable(R.string.error_parse, Endpoint.STATIC_GTFS.name), e);
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

    public static JsonObject findConnections(Context context, Stop fromStop, Stop toStop, String time) throws RequestException {
        return makeOwnRequest(context,
                "findConnections?fromStop=" + fromStop.parentStation + "&toStop=" + toStop.parentStation,
                JsonObject.class);
    }

    private static <T extends JsonElement> T makeOwnRequest(Context context, String endpoint, Class<T> type) throws RequestException {
        Endpoint resolved = Endpoint.APP_SERVER.resolve(endpoint);
        try (InputStream stream = readUrl(context, resolved)) {
            if (stream == null) {
                throw RequestException.reachError(resolved);
            }

            String output = new String(IOUtil.readAllBytes(stream), StandardCharsets.UTF_8);

            if (output.isBlank()) {
                throw RequestException.readError(resolved);
            }

            return new Gson().fromJson(output, type);
        } catch (RequestException e) {
            throw e;
        } catch (JsonSyntaxException e) {
            AppLog.e("Failed to parse response from " + resolved.url, e);
            throw RequestException.parseError(resolved);
        } catch (IOException e) {
            AppLog.e("IO error reading from " + resolved.url, e);
            throw new RequestException(Text.translatable(R.string.error_read, resolved.name), e);
        } catch (Exception e) {
            AppLog.e("Unexpected error reading from " + resolved.url, e);
            throw RequestException.unknownError(resolved, e);
        }
    }

    private static InputStream readUrl(Context context, Endpoint endpoint) throws RequestException {
        if (!hasNetwork(context)) {
            AppLog.d("assuming network is unreachable for " + endpoint.url);
            throw RequestException.offlineError(endpoint);
        }

        // TODO make this into a setting
        final int timeoutMs = 10_000;
        HttpURLConnection con = null;
        try {
            URL url = new URL(endpoint.url);

            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(timeoutMs);
            con.setReadTimeout(timeoutMs);

            int code = con.getResponseCode();
            if (code >= 400) {
                drainAndClose(con.getErrorStream());
                con.disconnect();
                throw RequestException.serverError(endpoint, code);
            }

            return con.getInputStream();
        } catch (SocketTimeoutException e) {
            if (con != null) con.disconnect();
            throw RequestException.timedOutError(endpoint, timeoutMs);
        } catch (IOException e) {
            if (con != null) con.disconnect();
            AppLog.e("Failed to reach " + endpoint.url, e);
            throw RequestException.reachError(endpoint);
        }
    }

    /** Reads and discards an error-stream body so the connection can be released, then closes it. */
    private static void drainAndClose(InputStream stream) {
        if (stream == null) return;
        try (stream) {
            IOUtil.readAllBytes(stream);
        } catch (IOException ignored) {
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
