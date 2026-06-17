package me.miran.libreinfo.util;

import android.util.Log;


public final class AppLog {

    private AppLog() {
    }

    public static void d(String message) {
        Log.d(callerTag(), message);
    }

    public static void w(String message) {
        Log.w(callerTag(), message);
    }

    public static void w(String message, Throwable throwable) {
        Log.w(callerTag(), message, throwable);
    }

    public static void e(String message) {
        Log.e(callerTag(), message);
    }

    public static void e(String message, Throwable throwable) {
        Log.e(callerTag(), message, throwable);
    }

    public static void d(String tag, String message) {
        Log.d(tag, message);
    }

    public static void w(String tag, String message) {
        Log.w(tag, message);
    }

    public static void w(String tag, String message, Throwable throwable) {
        Log.w(tag, message, throwable);
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
    }

    public static void e(String tag, String message, Throwable throwable) {
        Log.e(tag, message, throwable);
    }

    /** The simple name of the first caller outside {@code AppLog}, used as the log tag. */
    private static String callerTag() {
        return StackWalker.getInstance().walk(frames -> frames
                .map(StackWalker.StackFrame::getClassName)
                .filter(name -> !name.equals(AppLog.class.getName()))
                .findFirst()
                .map(AppLog::simpleTag)
                .orElse("App"));
    }

    private static String simpleTag(String className) {
        int dot = className.lastIndexOf('.');
        String simple = dot >= 0 ? className.substring(dot + 1) : className;

        // Collapse inner/anonymous classes (Outer$1, Outer$Inner) to the outer class name.
        int dollar = simple.indexOf('$');
        if (dollar >= 0) simple = simple.substring(0, dollar);

        return simple.isEmpty() ? "App" : simple;
    }
}
