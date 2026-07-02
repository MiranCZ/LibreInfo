package me.miran.libreinfo;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import me.miran.libreinfo.exception.AppException;
import me.miran.libreinfo.parsing.storage.manager.AppContainer;
import me.miran.libreinfo.util.AppLog;
import me.miran.libreinfo.parsing.storage.manager.StorageManager;
import me.miran.libreinfo.util.Settings;

public class Application extends android.app.Application {

    private static final String APP_UPDATE_WORKER_ID = "app-update";

    @Override
    public void onCreate() {
        super.onCreate();

        setupWorkers();

        Context context = this;
        new Thread(() -> {
            try {
                Settings.init(context);
                long ms = System.currentTimeMillis();
                new StorageManager(context).init();
                AppLog.d("Extracted data in " + (System.currentTimeMillis() - ms) + "ms");
            } catch (AppException e) {
                AppLog.e("Failed to initialize app data", e);
                AppContainer.INSTANCE.getStorageProvider().fail(e);
            }
        }).start();
    }

    private void setupWorkers() {
        setupIdStorageUpdateWorker();

        if (BuildConfig.AUTO_UPDATE_ENABLED) {
            setupAppUpdateWorker();
        } else {
            // Flag turned off drop the update work to be sure
            WorkManager.getInstance(this).cancelUniqueWork(APP_UPDATE_WORKER_ID);
        }
    }

    private void setupIdStorageUpdateWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .build();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(IdStorageUpdateWorker.class, 24, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "id-storage-update",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    private void setupAppUpdateWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .setRequiresStorageNotLow(true)
                .build();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(AppUpdateWorker.class, 24, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                APP_UPDATE_WORKER_ID,
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

}
