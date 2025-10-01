package com.example.mhdstuff.parsing.storage;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.example.mhdstuff.util.CacheHelper;
import com.example.mhdstuff.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public record IdStorage(LineStorage lineStorage, StopStorage stopStorage, PostStorage postStorage) {


    private static final Object mutex = new Object();
    private static final CountDownLatch readyLatch = new CountDownLatch(1);
    private static IdStorage storage;
    private static final List<Pair<Consumer<IdStorage>, Activity>> listeners = new ArrayList<>();
    private static boolean initCalled = false;

    public static void init(Context context) {
        if (initCalled) return;
        initCalled = true;
        Log.d("IdStorage", "Initializing...");
        long ms = System.currentTimeMillis();

        LineStorage lineStorage = LineStorage.parse(CacheHelper.getLineAliases(context));
        StopStorage stopStorage = StopStorage.parse(CacheHelper.getStops(context));
        PostStorage postStorage = PostStorage.parse(CacheHelper.getPosts(context), lineStorage, stopStorage);

        synchronized (mutex) {
            storage = new IdStorage(lineStorage, stopStorage, postStorage);
            readyLatch.countDown();


            for (var listener : listeners) {
                listener.right().runOnUiThread(() -> listener.left().accept(storage));
            }
            listeners.clear();
        }


        Log.d("IdStorage", "Initialized in " + (System.currentTimeMillis() - ms) + "ms");
    }

    /**
     * Guarantees that when the instance is passed onto the consumer it is being run on the main UI thread.
     * In the case the storage instance was already precessed it and this function is called from UI thread
     */
    public static void getInstanceOnUIThread(Consumer<IdStorage> consumer, Activity activity) {
        synchronized (mutex) {
            if (storage != null) {
                if (Looper.getMainLooper().isCurrentThread()) {
                    consumer.accept(storage);
                } else {
                    IdStorage saved = storage;
                    activity.runOnUiThread(() -> consumer.accept(saved));
                }
            } else {
                listeners.add(new Pair<>(consumer, activity));
            }
        }
    }

    public static IdStorage getInstance() {
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Should be called from background thread since it can be blocking");
        }
        if (!initCalled) {
            Log.w("IdStorage", "IdStorage called before starting to get initialized");
        }


        Log.d("IdStorage", "GetInstance called before initialized, waiting...");
        try {
            readyLatch.await();
            Log.d("IdStorage", "Freed up waiting thread");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        return storage;
    }


}
