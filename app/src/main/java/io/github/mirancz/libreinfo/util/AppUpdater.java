package io.github.mirancz.libreinfo.util;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.github.mirancz.libreinfo.AppUpdateWorker;
import io.github.mirancz.libreinfo.BuildConfig;

public class AppUpdater {

    private static final String WORK_NAME = "app-update";

    private static final String KEY_AUTO_UPDATE = "app_auto_update_enabled";
    private static final String KEY_LAST_CHECK = "app_update_last_check";
    private static final String KEY_PROMPT_SHOWN = "app_update_prompt_shown";


    private AppUpdater() {
    }

    public static boolean isAutoUpdateEnabled() {
        return Settings.get().getBoolean(KEY_AUTO_UPDATE, false);
    }

    public static void setAutoUpdateEnabled(Context context, boolean enabled) {
        Settings.get().putBoolean(KEY_AUTO_UPDATE, enabled).flush();

        if (enabled) {
            schedulePeriodic(context);
        } else {
            cancelPeriodic(context);
        }
    }

    public static long getLastCheckMillis() {
        return Settings.get().getLong(KEY_LAST_CHECK, 0L);
    }

    public static void recordCheck() {
        Settings.get().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).flush();
    }

    public static boolean isFirstRunPromptShown() {
        return Settings.get().getBoolean(KEY_PROMPT_SHOWN, false);
    }

    public static void markFirstRunPromptShown() {
        Settings.get().putBoolean(KEY_PROMPT_SHOWN, true).flush();
    }

    public static void schedulePeriodic(Context context) {
        if (!BuildConfig.AUTO_UPDATE_ENABLED) return;

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .setRequiresStorageNotLow(true)
                .build();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(AppUpdateWorker.class, 24, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .setInitialDelay(Duration.ofHours(12))
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    public static void cancelPeriodic(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}
