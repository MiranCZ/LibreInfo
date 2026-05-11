package me.miran.mhdstuff;

import android.content.Context;

import me.miran.mhdstuff.exception.AppException;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.util.CacheHelper;
import me.miran.mhdstuff.util.Settings;

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
                System.out.println("EXTRACTED IN " + (System.currentTimeMillis()-ms));
            } catch (AppException e) {
                e.printStackTrace();
                // FIXME silent exception
            }
            IdStorage.init(context);
        }).start();
    }
}
