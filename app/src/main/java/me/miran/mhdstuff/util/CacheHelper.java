package me.miran.mhdstuff.util;

import android.content.Context;
import android.util.Log;

import me.miran.mhdstuff.exception.AppException;
import me.miran.mhdstuff.util.request.RequestHelper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.tukaani.xz.XZInputStream;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import kotlin.text.Charsets;

public class CacheHelper {

    private static long serverUpdateTime = -1;

    public static void init(Context context) throws AppException {
        serverUpdateTime = RequestHelper.getLastStaticUpdate(context);
    }

    public static RandomAccessFile getRouteStopsRAF(Context context) throws AppException {
        try {
            return new RandomAccessFile(getCachedPath(context, "route_stops").toFile(), "r");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initializeData(Context context) throws AppException {
        if (!isCached(context, "data")) {

            // FIXME do buffered or something
            try {
                writeToCache(IOUtil.readAllBytes(RequestHelper.getData(context)), context, "data");
            } catch (Exception e) {
                throw new AppException("Failed to write to cache");
            }
        } else {
            return; // FIXME figure out if it is needed to extract the file, although tbf if someone deletes them its just a skill issue
        }

        try {
            BufferedInputStream buff = new BufferedInputStream(new FileInputStream(getCachedPath(context, "data").toFile()));

            XZInputStream inputStream = new XZInputStream(buff);

            DataInputStream is = new DataInputStream(inputStream);

            while (is.readBoolean()) {
                int nameLen = is.readInt();
                byte[] nameBytes = IOUtil.readNBytes(is, nameLen);
                String name = new String(nameBytes, StandardCharsets.UTF_8);

                Log.d("DataCache", "extracting "+name);
                int dataLen = is.readInt();

                byte[] buffer = new byte[1024];

                int len;

                FileOutputStream fos = new FileOutputStream(getCachedPath(context, name).toFile());
                while ((len = is.read(buffer, 0, Math.min(dataLen, buffer.length))) != -1) {
                    dataLen -= buffer.length;

                    fos.write(buffer, 0, len);
                    if (dataLen <= 0) break;
                }

                fos.close();
            }

            // write results
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DataInputStream getApi(Context context) throws AppException {
        return readCache(context, "api");
    }

    public static DataInputStream getStopMapping(Context context) throws AppException {
        return readCache(context, "stop_mapping");
    }

    public static DataInputStream getCalendar(Context context) throws AppException {
        return readCache(context, "calendar");
    }

    public static DataInputStream getCalendarDates(Context context) throws AppException {
        return readCache(context, "calendar_dates");
    }

    public static DataInputStream getStopTimes(Context context) throws AppException {
        return readCache(context, "stop_times");
    }

    public static DataInputStream getTrips(Context context) throws AppException {
        return readCache(context, "trips");
    }

    public static DataInputStream getStops(Context context) throws AppException {
        return readCache(context, "stops");
    }

    public static DataInputStream getLineAliases(Context context) throws AppException {
        return readCache(context, "lines");
    }

    public static DataInputStream getPosts(Context context) throws AppException {
        return readCache(context, "posts");
    }

    private static DataInputStream readCache(Context context, String... name) throws AppException {
        try {
            return new DataInputStream(new BufferedInputStream(new FileInputStream(getCachedPath(context, name).toFile())));
        } catch (Exception e) {
            throw new AppException("Failed to read cache file "+ Arrays.toString(name));
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
