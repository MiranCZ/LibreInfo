package me.miran;

import com.google.gson.Gson;
import me.miran.model.Vehicle;
import me.miran.model.VehicleInfo;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VehicleStoreTest {

    @Test
    void createsAndUpdatesTrips() {
        VehicleStore store = new VehicleStore();

        store.record(new Vehicle("v1", 5, 100, 10, 30));
        VehicleInfo info = store.getTrip(5, 100);
        assertNotNull(info);
        assertEquals(10, info.getLastStop());
        assertEquals(30, info.getDelay());
        assertEquals(1, info.getPreviousStops().size());

        // Same stop -> only delay updates, no new history entry.
        store.record(new Vehicle("v1", 5, 100, 10, 45));
        assertEquals(45, info.getDelay());
        assertEquals(1, info.getPreviousStops().size());

        // New stop -> history appended, lastStop/changedAt move.
        store.record(new Vehicle("v1", 5, 100, 11, 60));
        assertEquals(11, info.getLastStop());
        assertEquals(2, info.getPreviousStops().size());
        assertEquals(60, info.getDelay());

        assertEquals(1, store.getVehicles().size());
    }

    @Test
    void serializesWithCamelCaseFields() {
        VehicleStore store = new VehicleStore();
        store.record(new Vehicle("car42", 7, 200, 55, 12));

        String json = new Gson().toJson(store.getTrip(7, 200));
        assertTrue(json.contains("\"carId\":\"car42\""), json);
        assertTrue(json.contains("\"lastStop\":55"), json);
        assertTrue(json.contains("\"delay\":12"), json);
        assertTrue(json.contains("\"previousStops\""), json);
        assertTrue(json.contains("\"changedAt\""), json);
    }

    @Test
    void looksUpByLine() {
        VehicleStore store = new VehicleStore();
        store.record(new Vehicle("a", 1, 10, 99, 5));
        store.record(new Vehicle("b", 1, 11, 99, 8));

        Map<Integer, VehicleInfo> line = store.getLine(1);
        assertEquals(2, line.size());
        assertNull(store.getLine(999));
    }

    @Test
    void parsesStreamMessage() {
        Vehicle v = KordisStream.parse(
                "{\"attributes\":{\"ID\":\"v9\",\"LineID\":3,\"RouteID\":42,\"LastStopID\":77,\"Delay\":-15}}");
        assertEquals(new Vehicle("v9", 3, 42, 77, -15), v);
    }
}
