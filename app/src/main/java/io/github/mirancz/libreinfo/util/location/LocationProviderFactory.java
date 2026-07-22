package io.github.mirancz.libreinfo.util.location;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


/**
 * The default factory for {@link AppLocationProvider}s, can be overwritten by other flavours
 */
public class LocationProviderFactory {

    public static AppLocationProvider create(Context context) {
        if (googlePlayServicesAvailable(context)) {
            return new PlayServicesLocationProvider(context);
        } else {
            return new BuiltinLocationProvider(context);
        }
    }

    private static boolean googlePlayServicesAvailable(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

}
