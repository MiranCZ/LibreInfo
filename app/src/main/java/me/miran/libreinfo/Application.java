package me.miran.libreinfo;

import android.content.Context;

import me.miran.libreinfo.exception.AppException;
import me.miran.libreinfo.parsing.storage.manager.AppContainer;
import me.miran.libreinfo.parsing.storage.manager.IdStorage;
import me.miran.libreinfo.util.AppLog;
import me.miran.libreinfo.parsing.storage.manager.StorageManager;
import me.miran.libreinfo.util.Settings;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

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
}
