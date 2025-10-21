package com.example.mhdstuff.parsing.storage;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.exception.AppException;
import com.example.mhdstuff.util.CacheHelper;
import com.example.mhdstuff.util.Pair;
import com.example.mhdstuff.util.PreferencesHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public record IdStorage(LineStorage lineStorage, StopStorage stopStorage, PostStorage postStorage,
                        TripStorage tripStorage, RouteStopStorage routeStopStorage,
                        CalendarStorage calendarStorage, ApiStorage apiStorage) {


    private static final Object mutex = new Object();
    private static IdStorage storage;
    private static final Map<Class<?>, List<Pair<Consumer<?>, Activity>>> listeners = new HashMap<>();
    private static final Map<Class<?>, CountDownLatch> latches = new HashMap<>();
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static boolean initCalled = false;

    private static AppException error;


    public static void init(Context context) {
        init(context, false);
    }

    public static void init(Context context, boolean force) {
        if (force) {
            initCalled = false;
        }

        try {
            initInternal(context);
        } catch (AppException e) {
            e.printStackTrace();
            error = e;
        }
    }

    public static void onActivity(BaseActivity activity) {
        if (error != null) {
            error.showError(activity);
        }
    }

    private static void initInternal(Context context) throws AppException {
        if (initCalled) return;
        initCalled = true;
        Log.d("IdStorage", "Initializing...");
        long ms = System.currentTimeMillis();

        var preferences = context.getSharedPreferences("favStops", Context.MODE_PRIVATE);

        StopStorage stopStorage = StopStorage.parse(CacheHelper.getStops(context), new PreferencesHolder(preferences));
        onLoaded(StopStorage.class, stopStorage);

        LineStorage lineStorage = LineStorage.parse(CacheHelper.getLineAliases(context));
        onLoaded(LineStorage.class, lineStorage);

        PostStorage postStorage = PostStorage.parse(CacheHelper.getPosts(context), lineStorage, stopStorage);
        onLoaded(PostStorage.class, postStorage);

        ApiStorage apiStorage = ApiStorage.parse(CacheHelper.getApi(context));
        onLoaded(ApiStorage.class, apiStorage);

        TripStorage tripStorage = TripStorage.parse(CacheHelper.getTrips(context));
        RouteStopStorage routeStopStorage = RouteStopStorage.parse(
                CacheHelper.getStopTimes(context), CacheHelper.getRouteStopsRAF(context)
        );

        CalendarStorage calendarStorage = CalendarStorage.parse(
                CacheHelper.getCalendar(context), CacheHelper.getCalendarDates(context)
        );

        synchronized (mutex) {
            storage = new IdStorage(lineStorage, stopStorage, postStorage, tripStorage, routeStopStorage, calendarStorage, apiStorage);
            System.out.println("Saying on loaded");
            onLoaded(IdStorage.class, storage);
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
                listen(IdStorage.class, consumer, activity);
            }
        }
    }

    private static <T> void onLoaded(Class<T> clazz, T instance) {
        synchronized (mutex) {
            CountDownLatch latch = latches.get(clazz);
            if (latch != null) {
                latch.countDown();
            }
            instances.put(clazz, instance);

            for (var pair : listeners.getOrDefault(clazz, List.of())) {
                //noinspection unchecked
                Consumer<T> listener = (Consumer<T>) pair.left();

                System.out.println("LISTENER FREED ");
                pair.right().runOnUiThread(() -> listener.accept(instance));
            }
            listeners.getOrDefault(clazz, List.of()).clear();
        }
    }

    private static <T> void listen(Class<T> clazz, Consumer<T> consumer, Activity activity) {
        synchronized (mutex) {
            Log.d("IdStorage", "Listening for " + clazz);
            listeners.computeIfAbsent(clazz, k -> new ArrayList<>()).add(new Pair<>(consumer, activity));
            if (!latches.containsKey(clazz)) {
                latches.put(clazz, new CountDownLatch(1));
            }
        }
    }

    public static IdStorage getInstance() {
        if (!initCalled) {
            Log.w("IdStorage", "IdStorage called before starting to get initialized");
        }

        return getInstanceOf(IdStorage.class);
    }

    public static LineStorage getLineStorage() {
        return getInstanceOf(LineStorage.class);
    }

    public static StopStorage getStopStorage() {
        return getInstanceOf(StopStorage.class);
    }


    private static int waitId = 0;

    private static <T> T getInstanceOf(Class<T> clazz) {
        checkNotMainThread();
        synchronized (mutex) {
            Object instance = instances.get(clazz);
            if (instance != null) return (T) instance;

            if (!latches.containsKey(clazz)) {
                latches.put(clazz, new CountDownLatch(1));
            }
        }

        int id = waitId++;
        try {
            Log.d("IdStorage", "Waiting for " + clazz + " (" + id + ")");
            latches.get(clazz).await();
            Log.d("IdStorage", "Freed up waiting thread for " + clazz + " (" + id + ")");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        synchronized (mutex) {
            return (T) instances.get(clazz);
        }
    }

    private static void checkNotMainThread() {
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Should be called from background thread since it can be blocking");
        }
    }

}
