package io.github.mirancz.gtfs.ids;

import io.github.mirancz.gtfs.remap.Continuous;
import io.github.mirancz.gtfs.remap.IdType;

public record TransferEntry(
        @Continuous(IdType.STOP) String from_stop_id,
        @Continuous(IdType.STOP) String to_stop_id,
        int transfer_type,
        int min_transfer_time,
        @Continuous(IdType.TRIP) Integer from_trip_id,
        @Continuous(IdType.TRIP) Integer to_trip_id,
        Integer max_waiting_time
) {
}
