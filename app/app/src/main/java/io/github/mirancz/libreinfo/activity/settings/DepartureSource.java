package io.github.mirancz.libreinfo.activity.settings;

import io.github.mirancz.libreinfo.BuildConfig;

public enum DepartureSource {

    SERVER,
    LOCAL;


    public static final DepartureSource DEFAULT;

    static {
        if (BuildConfig.DEBUG) {
            DEFAULT = LOCAL;
        } else {
            DEFAULT = SERVER;
        }
    }

}
