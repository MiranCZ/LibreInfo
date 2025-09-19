package com.example.mhdstuff.activity.data;

import android.os.Looper;

import com.example.mhdstuff.parsing.types.Diversion;

public class DiversionDataHolder {

    private static Diversion diversion;

    public static Diversion getDiversion() {
        ensureOnMainThread();
        return diversion;
    }

    public static void setDiversion(Diversion diversion) {
        ensureOnMainThread();
        DiversionDataHolder.diversion = diversion;
    }


    private static void ensureOnMainThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Cannot call from other than main thread!");
        }
    }
}
