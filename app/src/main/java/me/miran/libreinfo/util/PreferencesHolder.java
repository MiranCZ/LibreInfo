package me.miran.libreinfo.util;

import android.content.SharedPreferences;

import java.util.Optional;

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

    public Optional<Integer> getInt(String key) {
        if (preferences.contains(key)) {
            return Optional.of(preferences.getInt(key, -1));
        }
        return Optional.empty();
    }

    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public Optional<Long> getLong(String key) {
        if (preferences.contains(key)) {
            return Optional.of(preferences.getLong(key, -1));
        }
        return Optional.empty();
    }

    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    public Optional<Float> getFloat(String key) {
        if (preferences.contains(key)) {
            return Optional.of(preferences.getFloat(key, -1));
        }
        return Optional.empty();
    }

    public float getFloat(String key, float defaultValue) {
        return preferences.getFloat(key, defaultValue);
    }

    public Optional<String> getString(String key) {
        if (preferences.contains(key)) {
            return Optional.ofNullable(preferences.getString(key, null));
        }
        return Optional.empty();
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public PreferencesHolder putBoolean(int key, boolean value) {
        return putBoolean(key+"", value);
    }

    public PreferencesHolder putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        return this;
    }

    public PreferencesHolder putInt(String key, int value) {
        editor.putInt(key, value);
        return this;
    }

    public PreferencesHolder putLong(String key, long value) {
        editor.putLong(key, value);
        return this;
    }

    public PreferencesHolder putFloat(String key, float value) {
        editor.putFloat(key, value);
        return this;
    }

    public PreferencesHolder putString(String key, String value) {
        editor.putString(key, value);
        return this;
    }

    public void flush() {
        editor.apply();
    }

}
