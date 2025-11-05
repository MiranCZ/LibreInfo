package com.example.mhdstuff;

import android.app.Activity;
import android.content.Context;

import com.example.mhdstuff.exception.AppException;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.util.CacheHelper;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = this;
        new Thread(() -> {
            try {
                CacheHelper.init();
            } catch (AppException e) {
                // FIXME silent exception
            }
            IdStorage.init(context);
        }).start();
    }
}
