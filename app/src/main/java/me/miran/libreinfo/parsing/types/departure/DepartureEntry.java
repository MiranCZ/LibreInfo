package me.miran.libreinfo.parsing.types.departure;

import me.miran.libreinfo.parsing.types.LineAlias;
import me.miran.libreinfo.parsing.types.TimeMark;

public record DepartureEntry(LineAlias line, String finalStop, int stopId, int postID, boolean lowFloor, TimeMark timeMark, int tripId, VehicleInfo vehicleInfo) {
}
