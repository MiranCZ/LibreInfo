package me.miran.libreinfo.activity.data;

import android.os.Looper;

import com.google.gson.JsonObject;

public class DelaysDataHolder {

    private static JsonObject delays;

    public static JsonObject getDelays() {
        ensureOnMainThread();
        return delays;
    }

    public static void setDelays(JsonObject delays) {
        ensureOnMainThread();
        DelaysDataHolder.delays = delays;
    }


    private static void ensureOnMainThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Cannot call from other than main thread!");
        }
    }
}
