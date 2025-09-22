package com.example.mhdstuff.util;

import android.content.Context;
import android.util.Log;

import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.util.request.RequestHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
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
        return readOrFetch("news.json", RequestHelper::getNews, context);
    }

    public static JsonArray getStops(Context context) {
        return readOrFetch("stops.json", RequestHelper::getStops, context);
    }

    public static JsonArray getPosts(Context context) {
        return readOrFetch("posts.json", RequestHelper::getPosts, context);
    }

    public static List<LineAlias> getLineAliases(Context context) {
        String name = "lines.json";
        if (isCached(name, context)) {
            Log.d("CacheHelper", "reading cached "+name);
            JsonArray array = readCache(name, JsonArray.class, context);

            return LineAlias.parseLineAliases(array);
        }

        Path gtfs = getGtfsEntry("routes.txt", context);

        Log.d("CacheHelper", "reading gtfs entry "+gtfs);
        try {
            String csvString = new String(Files.readAllBytes(gtfs), Charsets.UTF_8);

            System.out.println("READ STRING "+csvString.length());
            List<LineAlias> lines = LineAlias.parseCsv(csvString);
            System.out.println("PARSED "+lines.size());

            // TODO cache async?
            JsonArray result = new JsonArray();
            for (LineAlias line : lines) {
                result.add(line.toJson());
            }

            System.out.println("WRITING "+lines.size());
            writeCache(name, result, context);

            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Path getGtfsEntry(String fileName, Context context) {
        Path path = getCachedPath(context, "gtfs", fileName);
        if (!path.toFile().exists()) {
            ZipInputStream zis = RequestHelper.getStaticGTFS();
            try {
                extractZip(zis, context);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        return path;
    }

    private static void extractZip(ZipInputStream zis, Context context) throws IOException {
        ZipEntry entry;

        Path gtfsPath = getCachedPath(context, "gtfs");
        if (!gtfsPath.toFile().exists()) {
            gtfsPath.toFile().mkdir();
        }
        System.out.println("SHOUDL EXIST "+gtfsPath);

        while ((entry = zis.getNextEntry()) != null) {
            String fileName = entry.getName();
            System.out.println("\tread "+fileName);

            writeToCache(zis.readAllBytes(), context, "gtfs", fileName);
            zis.closeEntry();
        }

        zis.close();
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
            path = path.resolve(s);
        }

        return path;
    }

}
