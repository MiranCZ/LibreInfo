package io.github.mirancz.libreinfo.parsing.types;

/**
 * A point on the map. Conversions to Android/MapLibre types live in the app, see {@code LocationExt.kt}.
 */
public record Location(double latitude, double longitude) {

    public static final Location NONE = new Location(-1, -1);

}
