package com.example.mhdstuff.parsing.storage;

import android.content.Context;

import com.example.mhdstuff.util.CacheHelper;

public record IdStorage(LineStorage lineStorage, StopStorage stopStorage, PostStorage postStorage) {

    public static IdStorage create(Context context) {
        LineStorage lineStorage = LineStorage.parse(CacheHelper.getLineAliases(context));
        StopStorage stopStorage = StopStorage.parse(CacheHelper.getStops(context), lineStorage);
        PostStorage postStorage = PostStorage.parse(CacheHelper.getPosts(context), lineStorage, stopStorage);

        return new IdStorage(lineStorage, stopStorage, postStorage);
    }

}
