package me.miran.mhdstuff.parsing.types;

import me.miran.mhdstuff.util.request.soap.SoapSaneObject;
import com.google.gson.JsonObject;

import org.maplibre.android.geometry.LatLng;

import java.util.List;

public record Location(double latitude, double longitude) {


    public static final Location NONE = new Location(-1, -1);

    public static Location parse(JsonObject object) {
        double latitude = getLatitude(object);
        double longitude = getLongitude(object);

        return new Location(latitude, longitude);
    }

    public static Location parse(SoapSaneObject obj) {
        if (obj == null) return null;

        return new Location(obj.getDouble("Latitude"), obj.getDouble("Longitude"));
    }

    private static double getLatitude(JsonObject obj) {
        List<String> toTest = List.of("Latitude", "Lat", "lat");

        for (String s : toTest) {
            if (obj.has(s)) {
                return obj.get(s).getAsDouble();
            }
        }

        return -1;
    }

    private static double getLongitude(JsonObject obj) {
        List<String> toTest = List.of("Longitude", "Lng", "lng");

        for (String s : toTest) {
            if (obj.has(s)) {
                return obj.get(s).getAsDouble();
            }
        }

        return -1;

    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }
}
