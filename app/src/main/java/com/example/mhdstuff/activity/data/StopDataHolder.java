package com.example.mhdstuff.activity.data;

import android.os.Looper;

import com.example.mhdstuff.parsing.types.Stop;

public class StopDataHolder {

    private static Stop stop;

    public static Stop getStop() {
        ensureOnMainThread();
        return stop;
    }

    public static void setStop(Stop stop) {
        ensureOnMainThread();
        StopDataHolder.stop = stop;
    }


    private static void ensureOnMainThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Cannot call from other than main thread!");
        }
    }
}
