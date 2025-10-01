package com.example.mhdstuff.util;

import android.content.Context;
import android.util.Log;

import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.util.request.RequestHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import kotlin.text.Charsets;

public class CacheHelper {

    public static JsonArray getNews(Context context) {
        return CacheHelper.readOrFetchJson("news.json", RequestHelper::getNews, context);
    }

    public static DataInputStream getStops(Context context) {
        try {
            return readOrFetch(RequestHelper::getStops, context, "data", "stops");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DataInputStream getLineAliases(Context context) {
        try {
            return readOrFetch(RequestHelper::getLineAliases, context, "data", "lines");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonArray getPosts(Context context) {
        return CacheHelper.readOrFetchJson("posts.json", RequestHelper::getPosts, context);
    }

    private static DataInputStream readOrFetch(Callable<InputStream> fetchFunc, Context context, String... name) throws FileNotFoundException {
        if (!isCached(context, name)) {
            try {
                writeToCache(fetchFunc.call().readAllBytes(), context, name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return new DataInputStream(new BufferedInputStream(new FileInputStream(getCachedPath(context, name).toFile())));
    }

    private static JsonArray readOrFetchJson(String cacheName, Callable<JsonArray> fetchFunc, Context context) {
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


    private static boolean isCached(Context context, String... name) {
        // TODO check date
        return getCachedPath(context, name).toFile().exists();
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

        try {
            Files.write(path, bytes, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

}
