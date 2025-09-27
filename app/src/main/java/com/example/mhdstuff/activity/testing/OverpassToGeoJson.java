package com.example.mhdstuff.activity.testing;

import com.google.gson.*;
import java.util.*;

public class OverpassToGeoJson {
    private static final Gson GSON = new GsonBuilder().create();

    public static class GeoJsonPair {
        public final String routesGeoJson;
        public final String stopsGeoJson;
        public GeoJsonPair(String r, String s) { routesGeoJson = r; stopsGeoJson = s; }
    }

    public static GeoJsonPair convert(String overpassJson, String to) {
        JsonObject root = JsonParser.parseString(overpassJson).getAsJsonObject();
        JsonArray elements = root.has("elements") ? root.getAsJsonArray("elements") : new JsonArray();

        JsonArray routeFeatures = new JsonArray();
        JsonArray stopFeatures  = new JsonArray();

        Iterator<JsonElement> it = new Iterator<JsonElement>() {
            int index = 0;
            @Override
            public boolean hasNext() {
                return index < elements.size();
            }

            @Override
            public JsonElement next() {
                return elements.get(index++);
            }
        };

        while (it.hasNext()) {
            JsonElement el = it.next();
            JsonObject obj = el.getAsJsonObject();
            String type = obj.has("type") ? obj.get("type").getAsString() : "";

            // Ways -> LineString (use geometry array if present)
            if ("way".equals(type) && obj.has("geometry") && obj.get("role").getAsString().isEmpty()) {
                JsonArray geom = obj.getAsJsonArray("geometry");
                // Build coordinates array: [ [lon, lat], [lon, lat], ... ]
                JsonArray coords = new JsonArray();
                for (JsonElement ptEl : geom) {
                    JsonObject pt = ptEl.getAsJsonObject();
                    double lat = pt.get("lat").getAsDouble();
                    double lon = pt.get("lon").getAsDouble();
                    JsonArray coord = new JsonArray();
                    coord.add(lon);
                    coord.add(lat);
                    coords.add(coord);
                }
                // Create Feature
                JsonObject geometry = new JsonObject();
                geometry.addProperty("type", "LineString");
                geometry.add("coordinates", coords);

                JsonObject feature = new JsonObject();
                feature.addProperty("type", "Feature");
                feature.add("geometry", geometry);

                // Optionally add properties (id, tags)
                JsonObject props = new JsonObject();
                if (obj.has("id")) props.addProperty("id", obj.get("id").getAsLong());
                if (obj.has("tags")) props.add("tags", obj.get("tags").getAsJsonObject());
                feature.add("properties", props);

                routeFeatures.add(feature);
            }

            // Nodes -> Point (stops) if they have lat/lon or public_transport tags
            /*if ("node".equals(type) && obj.has("lat") && obj.has("lon")) {
                double lat = obj.get("lat").getAsDouble();
                double lon = obj.get("lon").getAsDouble();

                JsonObject geometry = new JsonObject();
                geometry.addProperty("type", "Point");
                JsonArray coord = new JsonArray();
                coord.add(lon);
                coord.add(lat);
                geometry.add("coordinates", coord);

                JsonObject feature = new JsonObject();
                feature.addProperty("type", "Feature");
                feature.add("geometry", geometry);

                JsonObject props = new JsonObject();
                if (obj.has("id")) props.addProperty("id", obj.get("id").getAsLong());
                if (obj.has("tags")) props.add("tags", obj.get("tags").getAsJsonObject());
                feature.add("properties", props);

                stopFeatures.add(feature);
            }*/
            if ("relation".equals(type)) {
                var tags = obj.get("tags").getAsJsonObject();
                if (/*tags.get("from").getAsString().equals(from) && */tags.get("to").getAsString().equals(to)) {
                    elements.addAll(obj.getAsJsonArray("members"));
                }
//                else {
//                    System.out.println(tags.get("from") + " ; "+tags.get("to"));
//                }
            }
        }

        JsonObject routesFC = new JsonObject();
        routesFC.addProperty("type", "FeatureCollection");
        routesFC.add("features", routeFeatures);

        JsonObject stopsFC = new JsonObject();
        stopsFC.addProperty("type", "FeatureCollection");
        stopsFC.add("features", stopFeatures);

        return new GeoJsonPair(GSON.toJson(routesFC), GSON.toJson(stopsFC));
    }
}
