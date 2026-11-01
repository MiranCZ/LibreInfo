package io.github.mirancz.gtfs.ids;

import io.github.mirancz.gtfs.remap.Continuous;

public record ApiEntry(
        int line_id,
        int route_id,
        int trip_id
) {
}
