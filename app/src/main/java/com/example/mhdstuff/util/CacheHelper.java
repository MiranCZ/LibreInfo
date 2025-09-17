package com.example.mhdstuff.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;

import kotlin.text.Charsets;

public class CacheHelper {

    public static JsonArray getNews(Context context) {
        return readOrFetch("news.json", RequestHelper::getNews, context);
    }

    public static JsonArray getLineAliases(Context context) {
        return readOrFetch("line_aliases.json", RequestHelper::getLineAliases, context);
    }

    public static JsonArray getStops(Context context) {
        return readOrFetch("stops.json", RequestHelper::getStops, context);
    }

    public static JsonArray getPosts(Context context) {
        return readOrFetch("posts.json", RequestHelper::getPosts, context);
    }


    private static JsonArray readOrFetch(String cacheName, Callable<JsonArray> fetchFunc, Context context) {
        if (isCached(cacheName, context)) {
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


    private static boolean isCached(String name, Context context) {
        // TODO check date
        return getCachedPath(name, context).toFile().exists();
    }

    private static void writeCache(String name, JsonElement element, Context context) {
        Path path = getCachedPath(name, context);


        try {
            Files.write(path, element.toString().getBytes(Charsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends JsonElement> T readCache(String name, Class<T> clazz, Context context) {
        Path path = getCachedPath(name, context);

        try {
            String data = new String(Files.readAllBytes(path), Charsets.UTF_8);

            return new Gson().fromJson(data, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getCachedPath(String name, Context context) {
        return context.getFilesDir().toPath().resolve(name);
    }

}
