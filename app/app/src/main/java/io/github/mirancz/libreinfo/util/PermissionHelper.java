package io.github.mirancz.libreinfo.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class PermissionHelper {

    public static boolean locationEnabled(Context context) {
        return permissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                permissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static boolean permissionGranted(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }




}
