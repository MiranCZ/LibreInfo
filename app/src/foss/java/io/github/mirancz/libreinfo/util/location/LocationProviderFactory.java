package io.github.mirancz.libreinfo.util.location;

import android.content.Context;

public class LocationProviderFactory {

    public static AppLocationProvider create(Context context) {
        return new BuiltinLocationProvider(context);
    }

}
