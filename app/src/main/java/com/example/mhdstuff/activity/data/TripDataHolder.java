package com.example.mhdstuff.activity.data;

import android.os.Looper;

import com.example.mhdstuff.parsing.types.Trip;

public class TripDataHolder {

    private static Trip trip;

    public static Trip getTrip() {
        ensureOnMainThread();
        return trip;
    }

    public static void setTrip(Trip trip) {
        ensureOnMainThread();
        TripDataHolder.trip = trip;
    }


    private static void ensureOnMainThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Cannot call from other than main thread!");
        }
    }

}
