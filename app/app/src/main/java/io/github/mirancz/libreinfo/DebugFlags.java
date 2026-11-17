package io.github.mirancz.libreinfo;

public class DebugFlags {


    private static final boolean USE_LOCALHOST = false;

    public static boolean shouldUseLocalhost() {
        //noinspection ConstantValue
        if (BuildConfig.BUILD_TYPE.equals("release")) {
            return false;
        } else {
            return USE_LOCALHOST;
        }
    }


}
