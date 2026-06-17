package me.miran.libreinfo;

import android.content.Context;

import me.miran.libreinfo.exception.AppException;
import me.miran.libreinfo.exception.StorageInitException;
import me.miran.libreinfo.parsing.storage.IdStorage;
import me.miran.libreinfo.util.AppLog;
import me.miran.libreinfo.util.CacheHelper;
import me.miran.libreinfo.util.Settings;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = this;
        new Thread(() -> {
            try {
                Settings.init(context);
                CacheHelper.init(context);
                long ms = System.currentTimeMillis();
                CacheHelper.initializeData(context);
                AppLog.d("Extracted data in " + (System.currentTimeMillis() - ms) + "ms");
            } catch (AppException e) {
                AppLog.e("Failed to initialize app data", e);
                IdStorage.fail(e);
                return;
            }

            IdStorage.init(context);
        }).start();
    }
}
