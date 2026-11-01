package io.github.mirancz.gtfs.ids;

import io.github.mirancz.gtfs.remap.Continuous;

import static io.github.mirancz.gtfs.remap.IdType.STOP;

public record StopEntry(
    @Continuous(STOP) String stop_id,
    String stop_name,
    double stop_lat,
    double stop_lon,
    String zoneId,
    int location_type,
    @Continuous(STOP) String parent_station,
    Integer wheelchair_boarding,
    String platform_code
) {
}
