package io.github.mirancz.gtfs;

import io.github.mirancz.gtfs.ids.*;

import java.util.List;

public record ParsedGtfs(
        List<GtfsApiEntry> api,
        List<GtfsStopEntry> stops,
        List<GtfsCalendarEntry> calendar,
        List<GtfsCalendarDateEntry> calendarDates,
        List<GtfsRouteEntry> routes,
        List<GtfsStopTimeEntry> stopTimes,
        List<GtfsTransferEntry> transfers,
        List<GtfsTripEntry> trips
) {
}
