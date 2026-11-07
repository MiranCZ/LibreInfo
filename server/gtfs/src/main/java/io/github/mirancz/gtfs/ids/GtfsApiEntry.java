package io.github.mirancz.gtfs.ids;

public record GtfsApiEntry(
        int line_id,
        int route_id,
        int trip_id
) {
}
