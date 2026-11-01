package io.github.mirancz.gtfs.remap;

public enum IdType {
    STOP(true),
    TRIP,
    SERVICE,
    BLOCK;

    public final boolean persist;

    IdType() {
        this(false);
    }

    IdType(boolean persist) {
        this.persist = persist;
    }
}
