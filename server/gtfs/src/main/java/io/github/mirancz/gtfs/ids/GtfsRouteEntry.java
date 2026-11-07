package io.github.mirancz.gtfs.ids;

public record GtfsRouteEntry(
        String route_id,
        int agency_id,
        String route_short_name,
        String route_long_name,
        int route_type,
        String route_color,
        String route_text_color
) {
}
