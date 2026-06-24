package me.miran.libreinfo;

import android.content.Context;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jspecify.annotations.NonNull;

import me.miran.libreinfo.exception.AppException;
import me.miran.libreinfo.parsing.storage.manager.StorageManager;
import me.miran.libreinfo.util.AppLog;

public class IdStorageUpdateWorker extends Worker {

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public IdStorageUpdateWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        try {
            boolean updated = new StorageManager(getApplicationContext()).update();
            AppLog.d("Update done, data " + (updated ? "refreshed" : "already current"));
            return Result.success();
        } catch (AppException e) {
            AppLog.w("Update failed, retrying ", e);
            return Result.retry();
        } catch (Exception e) {
            AppLog.e("Update failed, unexpected exception thrown, exiting ", e);
            return Result.failure();
        }
    }

}
