package com.example.mhdstuff.activity.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DataHolder {

    private static final AtomicInteger index = new AtomicInteger(0);
    private static final Map<Integer, Object> map = new HashMap<>();

    private DataHolder() {
    }


    public static <T> int createArg(T arg) {
        int ind = index.incrementAndGet();
        if (ind == -1) {
            ind = index.incrementAndGet();
        }

        map.put(ind, arg);
        return ind;
    }

    public static <T> T popArg(int index) {
        if (!map.containsKey(index)) return null;

        try {
            //noinspection unchecked
            return (T) map.remove(index);
        } catch (ClassCastException ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

}
