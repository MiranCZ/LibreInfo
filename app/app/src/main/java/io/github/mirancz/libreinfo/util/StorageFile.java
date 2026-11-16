package io.github.mirancz.libreinfo.util;

public enum StorageFile {

    API("api"),
    STOP_MAPPING("stop_mapping"),
    CALENDAR("calendar"),
    CALENDAR_DATES("calendar_dates"),
    ROUTES("routes"),
    TRIPS("trips"),
    STOPS("stops"),
    LINE_ALIASES("lines"),
    POSTS("posts");

    public final String fileName;

    StorageFile(String fileName) {
        this.fileName = fileName;
    }

}
