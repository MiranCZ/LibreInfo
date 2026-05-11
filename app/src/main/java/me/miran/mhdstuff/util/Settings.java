package me.miran.mhdstuff.util;

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



}
