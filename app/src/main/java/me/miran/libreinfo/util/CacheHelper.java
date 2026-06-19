package me.miran.libreinfo.util;

import static me.miran.libreinfo.util.StorageFile.*;

import android.content.Context;

import me.miran.libreinfo.R;
import me.miran.libreinfo.exception.AppException;
import me.miran.libreinfo.exception.ErrorType;
import me.miran.libreinfo.util.request.RequestHelper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.tukaani.xz.XZInputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import kotlin.text.Charsets;

public class CacheHelper {

    private static long serverUpdateTime = -1;
    private static boolean initCalled = false;

    public static void init(Context context) {
        if (initCalled) {
            throw new IllegalStateException("'init' called multiple times!");
        }
        initCalled = true;

        try {
            serverUpdateTime = RequestHelper.getLastStaticUpdate(context);
        } catch (AppException e) {
            AppLog.w("failed to get static update timestamp, using cached data", e);
        }
    }

    public static void initializeData(Context context) throws AppException {
        boolean freshFetch = false;

        if (!isCached(context, "data")) {
            freshFetch = true;

            try(var data = RequestHelper.getData(context)) {
                writeToCache(data, context, "data");
            } catch (Exception e) {
                throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
            }
        } else {
            boolean allExtraced = true;

            for (var file : StorageFile.values()) {
                if (!Files.exists(getCachedPath(context, file))) {
                    AppLog.d(file.fileName + " was not previously extracted, falling back");
                    allExtraced = false;
                    break;
                }
            }

            if (allExtraced) {
                AppLog.d("Everything was already extracted, exiting!");
                return;
            }
        }

        try(var dataIs = new FileInputStream(getCachedPath(context, "data").toFile())) {
            BufferedInputStream buff = new BufferedInputStream(dataIs);

            XZInputStream inputStream = new XZInputStream(buff);

            AppInputStream is = new AppInputStream(inputStream);

            while (is.readBoolean()) {
                String name = is.readString();
                String tmpName = name + ".tmp";

                Path tmpPath = getCachedPath(context, tmpName);
                Path finalPath = getCachedPath(context, name);


                AppLog.d("extracting "+name);
                int dataLen = is.readInt();

                if (Files.exists(tmpPath)) {
                    Files.delete(tmpPath);
                    AppLog.d("deleting stale temp entry for "+name);
                } else if (Files.exists(finalPath) && !freshFetch) {
                    AppLog.d(name + " already exists, skipping");

                    long skipped = 0;

                    while (skipped != dataLen) {
                        if (skipped > dataLen) {
                            throw new IOException("Skiped more than "+dataLen+" bytes ("+skipped+")");
                        }

                        long newSkipped = is.skip(dataLen-skipped);
                        if (newSkipped == 0) {
                            throw new IOException("Failed to skip "+dataLen+" bytes ("+skipped+")");
                        }

                        skipped += newSkipped;
                    }
                    continue;
                }


                byte[] buffer = new byte[1024];

                int len;

                try (FileOutputStream fos = new FileOutputStream(tmpPath.toFile())) {
                    while ((len = is.read(buffer, 0, Math.min(dataLen, buffer.length))) != -1) {
                        dataLen -= len;

                        fos.write(buffer, 0, len);
                        if (dataLen <= 0) break;
                    }

                    if (dataLen > 0) {
                        throw new IOException("Unexpected EOF extracting " + name + ", " + dataLen + " bytes short");
                    }
                }

                // rename to normal name only after fully written
                // (to mitigate half-written files when app is closed etc.)
                Files.move(
                        tmpPath,
                        finalPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (Exception e) {
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
        }
    }

    public static ByteBuffer getRouteStopsBuff(Context context) throws AppException {
        try(var channel = FileChannel.open(getCachedPath(context, ROUTE_STOPS), StandardOpenOption.READ)) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        } catch (IOException e) {
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
        }
    }

    public static AppInputStream getApi(Context context) throws AppException {
        return readCache(context, API);
    }

    public static AppInputStream getStopMapping(Context context) throws AppException {
        return readCache(context, STOP_MAPPING);
    }

    public static AppInputStream getCalendar(Context context) throws AppException {
        return readCache(context, CALENDAR);
    }

    public static AppInputStream getCalendarDates(Context context) throws AppException {
        return readCache(context, CALENDAR_DATES);
    }

    public static AppInputStream getStopTimes(Context context) throws AppException {
        return readCache(context, STOP_TIMES);
    }

    public static AppInputStream getTrips(Context context) throws AppException {
        return readCache(context, TRIPS);
    }

    public static AppInputStream getStops(Context context) throws AppException {
        return readCache(context, STOPS);
    }

    public static AppInputStream getLineAliases(Context context) throws AppException {
        return readCache(context, LINE_ALIASES);
    }

    public static AppInputStream getPosts(Context context) throws AppException {
        return readCache(context, POSTS);
    }


    private static AppInputStream readCache(Context context, StorageFile file) throws AppException {
        return readCache(context, file.fileName);
    }

    private static AppInputStream readCache(Context context, String... name) throws AppException {
        try {
            return new AppInputStream(new BufferedInputStream(new FileInputStream(getCachedPath(context, name).toFile())));
        } catch (Exception e) {
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
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
                AppLog.d("found old cache file "+ Arrays.toString(name));
                return false;
            }
        } catch (IOException e) {
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
        }

        return true;
    }

    public static void writeToCache(InputStream is, Context context, String... name) {
        Path path = getCachedPath(context, name);
        Path metaPath = getCachedMetaPath(context, name);

        var buffer = new byte[8192];
        try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            int n;
            while ((n = is.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Files.write(metaPath, getCacheTime(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] getCacheTime() {
        long current;

        if (serverUpdateTime != -1) {
            current = serverUpdateTime;
        } else {
            // safety fallback
            current = System.currentTimeMillis();
        }

        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (current & 0xFF);
            current >>= 8;
        }
        return bytes;
    }

    private static long bytesToLong(byte[] bytes) {
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


    private static Path getCachedPath(Context context, StorageFile file) {
        return getCachedPath(context, file.fileName);
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
