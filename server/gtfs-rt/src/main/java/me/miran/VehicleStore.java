package me.miran;

import com.google.gson.JsonObject;
import me.miran.model.StopDelay;
import me.miran.model.Vehicle;
import me.miran.model.VehicleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory store of live vehicle state, independent of how the data is sourced.
 *
 * <p>A single producer thread calls {@link #record(Vehicle)}; HTTP request threads read via the
 * {@link VehicleQuery} accessors. State is held in concurrent maps and {@link VehicleInfo}
 * publishes its fields safely, so no coarse locking is needed. Stale trips and vehicle snapshots
 * are pruned by a periodic garbage collector started with {@link #start()}.
 */
public class VehicleStore implements VehicleQuery {

    private static final Logger log = LoggerFactory.getLogger(VehicleStore.class);

    private static final long GC_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(1);
    private static final long MAX_AGE_MILLIS = TimeUnit.HOURS.toMillis(1);

    /** Latest raw {@code attributes} payload per vehicle, keyed by {@code id}, for {@code /vehicles}. */
    private final Map<String, JsonObject> vehicles = new ConcurrentHashMap<>();

    /** Wall-clock time of the latest observation per vehicle {@code id}, used to prune {@link #vehicles}. */
    private final Map<String, Long> vehicleSeenAt = new ConcurrentHashMap<>();

    /** {@code LineID -> RouteID -> VehicleInfo}. */
    private final Map<Integer, Map<Integer, VehicleInfo>> vehicleInfos = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "vehicle-store-gc");
                t.setDaemon(true);
                return t;
            });

    /** Starts the periodic garbage collector. */
    public void start() {
        scheduler.scheduleWithFixedDelay(this::runGc,
                GC_INTERVAL_SECONDS, GC_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /** Stops the garbage collector. */
    public void stop() {
        scheduler.shutdownNow();
    }

    /** Folds a fresh observation into the trip history and the latest-snapshot map. */
    public void record(Vehicle v) {
        record(v, System.currentTimeMillis());
    }

    /** Same as {@link #record(Vehicle)} but with an injectable clock. Package-private for testing. */
    void record(Vehicle v, long now) {
        Map<Integer, VehicleInfo> lineMap =
                vehicleInfos.computeIfAbsent(v.lineId(), k -> new ConcurrentHashMap<>());
        VehicleInfo info = lineMap.get(v.routeId());

        if (info == null) {
            info = new VehicleInfo(v.id(), v.lastStop(), now, v.delay());
            info.getPreviousStops().add(new StopDelay(v.lastStop(), v.delay()));
            lineMap.put(v.routeId(), info);
        } else if (info.getLastStop() != v.lastStop()) {
            info.setCarId(v.id());
            info.setLastStop(v.lastStop());
            info.setChangedAt(now);
            info.getPreviousStops().add(new StopDelay(v.lastStop(), v.delay()));
        }
        // The latest delay and contact time are always recorded, even when the vehicle hasn't moved stops.
        info.setDelay(v.delay());
        info.setLastSeen(now);

        vehicles.put(v.id(), v.raw());
        vehicleSeenAt.put(v.id(), now);
    }

    /** Drops trips and vehicle snapshots not seen within {@link #MAX_AGE_MILLIS}. */
    void runGc() {
        runGc(System.currentTimeMillis());
    }

    /** Same as {@link #runGc()} but with an injectable clock. Package-private for testing. */
    void runGc(long now) {
        try {
            int tripsBefore = countTrips();
            int vehiclesBefore = vehicles.size();

            for (Iterator<Map.Entry<Integer, Map<Integer, VehicleInfo>>> lineIt =
                 vehicleInfos.entrySet().iterator(); lineIt.hasNext(); ) {
                Map<Integer, VehicleInfo> lineMap = lineIt.next().getValue();
                lineMap.values().removeIf(info -> (now - info.getLastSeen()) >= MAX_AGE_MILLIS);
                if (lineMap.isEmpty()) {
                    lineIt.remove();
                }
            }

            vehicleSeenAt.forEach((id, seenAt) -> {
                if ((now - seenAt) >= MAX_AGE_MILLIS && vehicleSeenAt.remove(id, seenAt)) {
                    vehicles.remove(id);
                }
            });

            log.info("GC: trips {} -> {}, vehicles {} -> {}", tripsBefore, countTrips(), vehiclesBefore, vehicles.size());
        } catch (Exception e) {
            log.error("GC failed", e);
        }
    }

    private int countTrips() {
        int total = 0;
        for (Map<Integer, VehicleInfo> lineMap : vehicleInfos.values()) {
            total += lineMap.size();
        }
        return total;
    }

    // --- VehicleQuery ---

    @Override
    public Collection<JsonObject> getVehicles() {
        return Collections.unmodifiableCollection(vehicles.values());
    }

    @Override
    public Map<Integer, Map<Integer, VehicleInfo>> getVehicleInfos() {
        return Collections.unmodifiableMap(vehicleInfos);
    }

    @Override
    public Map<Integer, VehicleInfo> getLine(int lineId) {
        Map<Integer, VehicleInfo> lineMap = vehicleInfos.get(lineId);
        return lineMap == null ? null : Collections.unmodifiableMap(lineMap);
    }

    @Override
    public VehicleInfo getTrip(int lineId, int routeId) {
        Map<Integer, VehicleInfo> lineMap = vehicleInfos.get(lineId);
        return lineMap == null ? null : lineMap.get(routeId);
    }
}
