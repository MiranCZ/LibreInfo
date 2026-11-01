package io.github.mirancz.gtfs.ids;

import io.github.mirancz.gtfs.remap.Continuous;
import io.github.mirancz.gtfs.remap.IdType;

public record CalendarDateEntry(
        @Continuous(IdType.SERVICE) int service_id,
        String date,
        int exception_type
) {
}
