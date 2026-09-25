package io.github.mirancz.libreinfo.util;

import android.content.Context;

import io.github.mirancz.libreinfo.parsing.types.stop.StopId;

public class FavouriteStops {

    private static PreferencesHolder holder = null;

    public static boolean isFavourite(StopId stopId) {
        return get().getBoolean(stopId.internal(), false);
    }

    public static void setFavourite(StopId stopId, boolean favourite) {
        get().putBoolean(stopId.internal(), favourite).flush();
    }

    public static void init(Context context) {
        if (holder != null) {
            throw new IllegalStateException("Already initialized!");
        }

        var preferences = context.getSharedPreferences("favStops", Context.MODE_PRIVATE);
        holder = new PreferencesHolder(preferences);
    }

    private static PreferencesHolder get() {
        if (holder == null) throw new IllegalStateException("Settings not initialized!");
        return holder;
    }
    
}
