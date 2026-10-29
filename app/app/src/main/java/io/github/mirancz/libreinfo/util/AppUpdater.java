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


    private AppUpdater() {
    }

    public static boolean isAutoUpdateEnabled() {
        return AppSettings.Updates.getAutoUpdateEnabled();
    }

    public static void setAutoUpdateEnabled(Context context, boolean enabled) {
        AppSettings.Updates.setAutoUpdateEnabled(enabled);

        if (enabled) {
            schedulePeriodic(context);
        } else {
            cancelPeriodic(context);
        }
    }

    public static long getLastCheckMillis() {
        return AppSettings.Updates.getUpdateLastCheck();
    }

    public static void recordCheck() {
        AppSettings.Updates.setUpdateLastCheck(System.currentTimeMillis());
    }

    public static boolean isFirstRunPromptShown() {
        return AppSettings.Updates.getUpdatePromptShown();
    }

    public static void markFirstRunPromptShown() {
        AppSettings.Updates.setUpdatePromptShown(true);
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
