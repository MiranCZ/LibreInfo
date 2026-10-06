package io.github.mirancz.libreinfo.parsing.types.departure;

import io.github.mirancz.libreinfo.parsing.types.LineAlias;
import io.github.mirancz.libreinfo.parsing.types.TimeMark;

public record DepartureEntry(LineAlias line, String finalStop, int stopId, int postID, boolean lowFloor, TimeMark timeMark, int tripId, VehicleInfo vehicleInfo) {
}
