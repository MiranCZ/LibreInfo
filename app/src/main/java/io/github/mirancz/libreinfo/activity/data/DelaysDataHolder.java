package io.github.mirancz.libreinfo.activity.data;

import android.os.Looper;

import io.github.mirancz.libreinfo.parsing.types.response.RouteDelaysResponse;

public class DelaysDataHolder {

    private static RouteDelaysResponse delays;

    public static RouteDelaysResponse getDelays() {
        ensureOnMainThread();
        return delays;
    }

    public static void setDelays(RouteDelaysResponse delays) {
        ensureOnMainThread();
        DelaysDataHolder.delays = delays;
    }


    private static void ensureOnMainThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Cannot call from other than main thread!");
        }
    }
}
