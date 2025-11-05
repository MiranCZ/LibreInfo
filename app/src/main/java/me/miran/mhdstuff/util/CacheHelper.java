package me.miran.mhdstuff.util;

import android.content.Context;
import android.util.Log;

import me.miran.mhdstuff.exception.AppException;
import me.miran.mhdstuff.util.request.RequestHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.Callable;

import kotlin.text.Charsets;

public class CacheHelper {

    private static long serverUpdateTime = -1;

    public static void init() throws AppException {
        serverUpdateTime = RequestHelper.getLastStaticUpdate();
    }

    public static JsonArray getNews(Context context) throws AppException {
        return CacheHelper.readOrFetchJson("news.json", RequestHelper::getNews, context);
    }

    public static RandomAccessFile getRouteStopsRAF(Context context) throws AppException {
        if (!isCached(context, "data", "route_stops")) {
            try {
                writeToCache(IOUtil.readAllBytes(RequestHelper.getRouteStops()), context, "data", "route_stops");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try {
            return new RandomAccessFile(getCachedPath(context, "data", "route_stops").toFile(), "r");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static DataInputStream getApi(Context context) throws AppException {
        return readOrFetch(RequestHelper::getApi, context, "data", "api");
    }

    public static DataInputStream getCalendar(Context context) throws AppException {
        return readOrFetch(RequestHelper::getCalendar, context, "data", "calendar");
    }

    public static DataInputStream getCalendarDates(Context context) throws AppException {
        return readOrFetch(RequestHelper::getCalendarDates, context, "data", "calendar_dates");
    }

    public static DataInputStream getStopTimes(Context context) throws AppException {
        return readOrFetch(RequestHelper::getStopTimes, context, "data", "stop_times");
    }

    public static DataInputStream getTrips(Context context) throws AppException {
        return readOrFetch(RequestHelper::getTrips, context, "data", "trips");
    }

    public static DataInputStream getStops(Context context) throws AppException {
        return readOrFetch(RequestHelper::getStops, context, "data", "stops");
    }

    public static DataInputStream getLineAliases(Context context) throws AppException {
        return readOrFetch(RequestHelper::getLineAliases, context, "data", "lines");
    }

    public static DataInputStream getPosts(Context context) throws AppException {
        return readOrFetch(RequestHelper::getPosts, context, "data", "posts");
    }

    private static DataInputStream readOrFetch(Callable<InputStream> fetchFunc, Context context, String... name) throws AppException {
        if (!isCached(context, name)) {
            try {
                writeToCache(IOUtil.readAllBytes(fetchFunc.call()), context, name);
            } catch (Exception e) {
                throw new AppException("Failed to write to cache");
            }
        }

        try {
            return new DataInputStream(new BufferedInputStream(new FileInputStream(getCachedPath(context, name).toFile())));
        } catch (Exception e) {
            throw new AppException("Failed to read cache file "+ Arrays.toString(name));
        }
    }

    private static JsonArray readOrFetchJson(String cacheName, Callable<JsonArray> fetchFunc, Context context) throws AppException {
        if (isCached(context, cacheName)) {
            System.out.println("Using cached version of " + cacheName);
            return readCache(cacheName, JsonArray.class, context);
        }

        try {
            JsonArray result = fetchFunc.call();
            Log.e("CacheHelper", "Fetched " + cacheName + " from server");
            writeCache(cacheName, result, context);

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static boolean isCached(Context context, String... name) throws AppException {
        Path metaPath = getCachedMetaPath(context, name);
        if (!getCachedPath(context, name).toFile().exists() || !metaPath.toFile().exists()) {
            return false;
        }
        if (serverUpdateTime == -1) return true;

        try {
            byte[] timeB = Files.readAllBytes(metaPath);
            long time = bytesToLong(timeB);

            if (time < serverUpdateTime) {
                Log.d("CacheHelper", "found old cache file "+ Arrays.toString(name));
                return false;
            }
        } catch (IOException e) {
            throw new AppException("Failed to read metadata file");
        }

        return true;
    }

    private static void writeCache(String name, JsonElement element, Context context) {
        Path path = getCachedPath(context, name);

        try {
            Files.write(path, element.toString().getBytes(Charsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeToCache(byte[] bytes, Context context, String... name) {
        Path path = getCachedPath(context, name);
        Path metaPath = getCachedMetaPath(context, name);

        try {
            Files.write(path, bytes, StandardOpenOption.CREATE);
            Files.write(metaPath, currentTimeBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] currentTimeBytes() {
        long current = System.currentTimeMillis();

        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (current & 0xFF);
            current >>= 8;
        }
        return bytes;
    }

    public static long bytesToLong(byte[] bytes) {
        if (bytes.length != 8)
            throw new IllegalArgumentException("Byte array must be 8 bytes long.");

        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (bytes[i] & 0xFF);
        }
        return value;
    }

    private static <T extends JsonElement> T readCache(String name, Class<T> clazz, Context context) {
        Path path = getCachedPath(context, name);

        try {
            String data = new String(Files.readAllBytes(path), Charsets.UTF_8);

            return new Gson().fromJson(data, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getCachedPath(Context context, String... name) {
        Path path = context.getFilesDir().toPath();

        for (String s : name) {
            if (!path.toFile().exists()) {
                path.toFile().mkdir();
            }
            path = path.resolve(s);
        }

        return path;
    }

    private static Path getCachedMetaPath(Context context, String... name) {
        return getCachedPath(context,name).getParent().resolve(name[name.length-1] + ".meta");
    }

}
