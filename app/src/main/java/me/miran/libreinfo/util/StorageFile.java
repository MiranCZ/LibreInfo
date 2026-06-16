package me.miran.libreinfo.util;

public enum StorageFile {

    API("api"),
    STOP_MAPPING("stop_mapping"),
    CALENDAR("calendar"),
    CALENDAR_DATES("calendar_dates"),
    STOP_TIMES("stop_times"),
    TRIPS("trips"),
    STOPS("stops"),
    LINE_ALIASES("lines"),
    POSTS("posts"),
    ROUTE_STOPS("route_stops");

    public final String fileName;

    StorageFile(String fileName) {
        this.fileName = fileName;
    }

}
