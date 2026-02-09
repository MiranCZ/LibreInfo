package me.miran.mhdstuff.parsing.types.departure;

import me.miran.mhdstuff.parsing.types.LineAlias;
import me.miran.mhdstuff.parsing.types.TimeMark;

public record DepartureEntry(LineAlias line, String finalStop, int stopId, int postID, boolean lowFloor, TimeMark timeMark, int tripId, VehicleInfo vehicleInfo) {
}
