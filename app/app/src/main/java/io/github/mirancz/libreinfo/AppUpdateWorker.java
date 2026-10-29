package io.github.mirancz.libreinfo;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import io.github.mirancz.libreinfo.util.UpdateDownloader;


public class AppUpdateWorker extends Worker {

    /**
     * @param context   The application {@link Context}
     * @param workerParams Parameters to set up the internal state of this worker
     */
    public AppUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        var result = UpdateDownloader.checkAndDownload(getApplicationContext());

        switch (result) {
            case UP_TO_DATE, DOWNLOADED -> {
                return Result.success();
            }
            case FAILURE -> {
                return Result.retry();
            }

            default -> {
                return Result.failure();
            }
        }
    }




}
