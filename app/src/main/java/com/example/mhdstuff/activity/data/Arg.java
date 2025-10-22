package com.example.mhdstuff.activity.data;

import java.util.concurrent.Callable;

public class Arg<T> {

    private final Callable<T> getter;
    private T value = null;
    private boolean set = false;

    public Arg(Callable<T> getter) {
        this.getter = getter;
    }

    public T get() {
        if (!set) {
            initialize();
        }

        return value;
    }

    public void initialize() {
        set = true;
        try {
            value = getter.call();
        } catch (Exception ignored) {
        }
    }



}
