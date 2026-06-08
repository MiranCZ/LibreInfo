package me.miran.libreinfo.util;

import android.content.Context;

public class Settings {

    private static PreferencesHolder holder = null;

    public static void init(Context context) {
        if (holder != null) {
            throw new IllegalStateException("Already initialized!");
        }

        var preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        holder = new PreferencesHolder(preferences);
    }

    public static PreferencesHolder get() {
        if (holder == null) throw new IllegalStateException("Settings not initialized!");
        return holder;
    }

}
