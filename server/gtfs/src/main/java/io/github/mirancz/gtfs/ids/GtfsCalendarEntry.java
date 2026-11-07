package io.github.mirancz.gtfs.ids;

import io.github.mirancz.gtfs.remap.Continuous;
import io.github.mirancz.gtfs.remap.IdType;

public record GtfsCalendarEntry(
        @Continuous(IdType.SERVICE) int service_id,
        byte monday,
        byte tuesday,
        byte wednesday,
        byte thursday,
        byte friday,
        byte saturday,
        byte sunday,
        String start_date,
        String end_date
) {
}
