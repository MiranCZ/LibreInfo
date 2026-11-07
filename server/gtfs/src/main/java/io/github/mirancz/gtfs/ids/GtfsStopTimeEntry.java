package io.github.mirancz.gtfs.ids;

import io.github.mirancz.gtfs.remap.Continuous;
import io.github.mirancz.gtfs.remap.IdType;

public record GtfsStopTimeEntry(
        @Continuous(IdType.TRIP) int trip_id,
        String arrival_time,
        String departure_time,
        @Continuous(IdType.STOP) String stop_id,
        int stop_sequence,
        Integer pickup_type,
        Integer drop_off_bike,
        Integer bikes_allowed
) {
}
