package me.miran;

import me.miran.model.Vehicle;
import me.miran.model.VehicleInfo;

import java.util.Collection;
import java.util.Map;

/**
 * Read-only view of the vehicle state, as needed by the HTTP endpoints. Hides the write and
 * lifecycle surface of {@link VehicleStore} from the presentation layer.
 */
public interface VehicleQuery {

    /** Latest snapshot of every known vehicle. */
    Collection<Vehicle> getVehicles();

    /** All trips as {@code LineID -> RouteID -> VehicleInfo}. */
    Map<Integer, Map<Integer, VehicleInfo>> getVehicleInfos();

    /** Route map for a line, or {@code null} if the line is unknown. */
    Map<Integer, VehicleInfo> getLine(int lineId);

    /** A single trip, or {@code null} if the line/route is unknown. */
    VehicleInfo getTrip(int lineId, int routeId);
}
