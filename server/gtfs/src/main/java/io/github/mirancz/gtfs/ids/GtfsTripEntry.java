package io.github.mirancz.gtfs.ids;

import io.github.mirancz.gtfs.remap.Continuous;
import io.github.mirancz.gtfs.remap.IdType;

public record GtfsTripEntry(
        String route_id,
        @Continuous(IdType.SERVICE) int service_id,
        @Continuous(IdType.TRIP) int trip_id,
        String trip_headsign,
        int wheelchair_accessible,
        @Continuous(IdType.BLOCK) Integer block_id,
        int direction_id,
        int bikes_allowed,
        int exceptional
) {
}
