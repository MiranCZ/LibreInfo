package me.miran.mhdstuff.util;

import android.content.SharedPreferences;

public class PreferencesHolder {

    public static final PreferencesHolder NONE = new PreferencesHolder(new DummySharedPreferences());

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public PreferencesHolder(SharedPreferences sharedPreferences) {
        this.preferences = sharedPreferences;
        this.editor = sharedPreferences.edit();
    }

    public boolean getBoolean(int key, boolean defaultValue) {
        return getBoolean(key+"", defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public PreferencesHolder putBoolean(int key, boolean value) {
        return putBoolean(key+"", value);
    }

    public PreferencesHolder putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        return this;
    }

    public void flush() {
        editor.apply();
    }

}
