package com.example.mhdstuff;

import android.app.Activity;
import android.content.Context;

import com.example.mhdstuff.parsing.storage.IdStorage;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = this;
        new Thread(() -> IdStorage.init(context)).start();
    }
}
