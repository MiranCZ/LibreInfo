package io.github.mirancz.libreinfo.util;

import android.content.Context;

public class FavouriteStops {

    private static PreferencesHolder holder = null;

    public static boolean isFavourite(int stopId) {
        return get().getBoolean(stopId, false);
    }

    public static void setFavourite(int stopId, boolean favourite) {
        get().putBoolean(stopId, favourite).flush();
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
