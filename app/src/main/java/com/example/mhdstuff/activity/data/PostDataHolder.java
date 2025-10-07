package com.example.mhdstuff.activity.data;

import android.os.Looper;

import com.example.mhdstuff.parsing.types.Post;

public class PostDataHolder {

    private static Post post;

    public static Post getPost() {
        ensureOnMainThread();
        return post;
    }

    public static void setPost(Post post) {
        ensureOnMainThread();
        PostDataHolder.post = post;
    }


    private static void ensureOnMainThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Cannot call from other than main thread!");
        }
    }

}
