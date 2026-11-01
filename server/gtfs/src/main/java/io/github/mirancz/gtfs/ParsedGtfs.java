package io.github.mirancz.gtfs;

import io.github.mirancz.gtfs.ids.*;

import java.util.List;

public record ParsedGtfs(
        List<ApiEntry> api,
        List<StopEntry> stops,
        List<CalendarEntry> calendar,
        List<CalendarDateEntry> calendarDates,
        List<RouteEntry> routes,
        List<StopTimeEntry> stopTimes,
        List<TransferEntry> transfers,
        List<TripEntry> trips
) {
}
