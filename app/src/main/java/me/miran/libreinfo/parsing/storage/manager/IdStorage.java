package me.miran.libreinfo.parsing.storage.manager;

import me.miran.libreinfo.parsing.storage.ApiStorage;
import me.miran.libreinfo.parsing.storage.AppStorage;
import me.miran.libreinfo.parsing.storage.CalendarStorage;
import me.miran.libreinfo.parsing.storage.LineStorage;
import me.miran.libreinfo.parsing.storage.PostStorage;
import me.miran.libreinfo.parsing.storage.RouteStopStorage;
import me.miran.libreinfo.parsing.storage.StopMapper;
import me.miran.libreinfo.parsing.storage.StopStorage;
import me.miran.libreinfo.parsing.storage.TripStorage;

public record IdStorage(LineStorage lineStorage, StopStorage stopStorage, PostStorage postStorage,
                        TripStorage tripStorage, RouteStopStorage routeStopStorage,
                        CalendarStorage calendarStorage, ApiStorage apiStorage,
                        StopMapper stopMapper) implements AppStorage {


}
