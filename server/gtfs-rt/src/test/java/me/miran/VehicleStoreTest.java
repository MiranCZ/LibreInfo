package me.miran;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.miran.model.Vehicle;
import me.miran.model.VehicleInfo;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class VehicleStoreTest {

    /** Builds a Vehicle with a representative raw {@code attributes} payload. */
    private static Vehicle vehicle(String id, int line, int route, int stop, int delay) {
        JsonObject raw = new JsonObject();
        raw.addProperty("ID", id);
        raw.addProperty("LineID", line);
        raw.addProperty("RouteID", route);
        raw.addProperty("LastStopID", stop);
        raw.addProperty("Delay", delay);
        return new Vehicle(id, line, route, stop, delay, raw);
    }

    @Test
    void createsAndUpdatesTrips() {
        VehicleStore store = new VehicleStore();

        store.record(vehicle("v1", 5, 100, 10, 30));
        VehicleInfo info = store.getTrip(5, 100);
        assertNotNull(info);
        assertEquals(10, info.getLastStop());
        assertEquals(30, info.getDelay());
        assertEquals(1, info.getPreviousStops().size());

        // Same stop -> only delay updates, no new history entry.
        store.record(vehicle("v1", 5, 100, 10, 45));
        assertEquals(45, info.getDelay());
        assertEquals(1, info.getPreviousStops().size());

        // New stop -> history appended, lastStop/changedAt move.
        store.record(vehicle("v1", 5, 100, 11, 60));
        assertEquals(11, info.getLastStop());
        assertEquals(2, info.getPreviousStops().size());
        assertEquals(60, info.getDelay());

        assertEquals(1, store.getVehicles().size());
    }

    @Test
    void serializesWithSnakeCaseFields() {
        VehicleStore store = new VehicleStore();
        store.record(vehicle("car42", 7, 200, 55, 12));

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        String json = gson.toJson(store.getTrip(7, 200));
        assertTrue(json.contains("\"car_id\":\"car42\""), json);
        assertTrue(json.contains("\"last_stop\":55"), json);
        assertTrue(json.contains("\"delay\":12"), json);
        assertTrue(json.contains("\"previous_stops\""), json);
        assertTrue(json.contains("\"changed_at\""), json);
    }

    @Test
    void serializesVehiclesAsRawPassthrough() {
        VehicleStore store = new VehicleStore();
        Vehicle v = KordisStream.parse(
                "{\"attributes\":{\"ID\":\"21302\",\"LineID\":602,\"RouteID\":127,"
                        + "\"LastStopID\":1696,\"Delay\":0.0,\"Lat\":49.18,\"LineName\":\"602\"}}");
        store.record(v);

        // The snake_case Gson must not rewrite the verbatim WebSocket keys.
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        String json = gson.toJson(store.getVehicles());
        assertTrue(json.contains("\"ID\":\"21302\""), json);
        assertTrue(json.contains("\"LineName\":\"602\""), json);
        assertTrue(json.contains("\"Lat\":49.18"), json);
        assertTrue(json.contains("\"Delay\":0.0"), json);
    }

    @Test
    void looksUpByLine() {
        VehicleStore store = new VehicleStore();
        store.record(vehicle("a", 1, 10, 99, 5));
        store.record(vehicle("b", 1, 11, 99, 8));

        Map<Integer, VehicleInfo> line = store.getLine(1);
        assertEquals(2, line.size());
        assertNull(store.getLine(999));
    }

    @Test
    void parsesStreamMessage() {
        Vehicle v = KordisStream.parse(
                "{\"attributes\":{\"ID\":\"v9\",\"LineID\":3,\"RouteID\":42,\"LastStopID\":77,\"Delay\":-15}}");
        assertEquals("v9", v.id());
        assertEquals(3, v.lineId());
        assertEquals(42, v.routeId());
        assertEquals(77, v.lastStop());
        assertEquals(-15, v.delay());
        assertEquals("v9", v.raw().get("ID").getAsString());
    }

    @Test
    void gcPrunesStaleTripsAndVehicleSnapshots() {
        VehicleStore store = new VehicleStore();
        long t0 = 1_000_000_000L;
        store.record(vehicle("v1", 5, 100, 10, 30), t0);

        assertEquals(1, store.getVehicles().size());
        assertNotNull(store.getTrip(5, 100));

        // Within the max age: nothing is pruned.
        store.runGc(t0 + TimeUnit.MINUTES.toMillis(30));
        assertEquals(1, store.getVehicles().size());
        assertNotNull(store.getTrip(5, 100));

        // Past the max age: both the trip and the raw vehicle snapshot are dropped.
        store.runGc(t0 + TimeUnit.HOURS.toMillis(2));
        assertNull(store.getTrip(5, 100));
        assertNull(store.getLine(5));
        assertEquals(0, store.getVehicles().size());
    }

    @Test
    void delayOnlyUpdatesKeepTripAliveAcrossGc() {
        VehicleStore store = new VehicleStore();
        long t0 = 1_000_000_000L;
        store.record(vehicle("v1", 5, 100, 10, 30), t0);

        // 50 min later, same stop, only the delay changes: changedAt stays put, last contact advances.
        long t1 = t0 + TimeUnit.MINUTES.toMillis(50);
        store.record(vehicle("v1", 5, 100, 10, 45), t1);
        assertEquals(t0, store.getTrip(5, 100).getChangedAt());

        // 70 min after the *stop change* but only 20 min after the *last contact*: must survive.
        store.runGc(t0 + TimeUnit.MINUTES.toMillis(70));
        assertNotNull(store.getTrip(5, 100));
        assertEquals(1, store.getVehicles().size());

        // 70 min after the last contact: now stale, so pruned.
        store.runGc(t1 + TimeUnit.MINUTES.toMillis(70));
        assertNull(store.getTrip(5, 100));
        assertEquals(0, store.getVehicles().size());
    }
}
