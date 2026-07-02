package io.github.mirancz.libreinfo.parsing.storage.manager;

import io.github.mirancz.libreinfo.parsing.storage.ApiStorage;
import io.github.mirancz.libreinfo.parsing.storage.AppStorage;
import io.github.mirancz.libreinfo.parsing.storage.CalendarStorage;
import io.github.mirancz.libreinfo.parsing.storage.LineStorage;
import io.github.mirancz.libreinfo.parsing.storage.PostStorage;
import io.github.mirancz.libreinfo.parsing.storage.RouteStopStorage;
import io.github.mirancz.libreinfo.parsing.storage.StopMapper;
import io.github.mirancz.libreinfo.parsing.storage.StopStorage;
import io.github.mirancz.libreinfo.parsing.storage.TripStorage;

public record IdStorage(LineStorage lineStorage, StopStorage stopStorage, PostStorage postStorage,
                        TripStorage tripStorage, RouteStopStorage routeStopStorage,
                        CalendarStorage calendarStorage, ApiStorage apiStorage,
                        StopMapper stopMapper) implements AppStorage {


}
