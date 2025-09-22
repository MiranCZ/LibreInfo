package com.example.mhdstuff.parsing.storage;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.example.mhdstuff.util.CacheHelper;

import java.util.concurrent.CountDownLatch;

public record IdStorage(LineStorage lineStorage, StopStorage stopStorage, PostStorage postStorage) {


    private static final CountDownLatch readyLatch = new CountDownLatch(1);
    private static IdStorage storage;

    private static boolean initCalled = false;

    public static void init(Context context) {
        if (initCalled) return;
        initCalled = true;
        Log.d("IdStorage", "Initializing...");
        long ms = System.currentTimeMillis();

        LineStorage lineStorage = new LineStorage(CacheHelper.getLineAliases(context));
        StopStorage stopStorage = StopStorage.parse(CacheHelper.getStops(context), lineStorage);
        PostStorage postStorage = PostStorage.parse(CacheHelper.getPosts(context), lineStorage, stopStorage);

        storage = new IdStorage(lineStorage, stopStorage, postStorage);
        readyLatch.countDown();


        Log.d("IdStorage", "Initialized in " + (System.currentTimeMillis() - ms) + "ms");
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
