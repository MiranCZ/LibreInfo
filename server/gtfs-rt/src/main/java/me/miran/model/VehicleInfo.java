package me.miran.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Live state of a single trip (a {@code LineID} + {@code RouteID} pair), built up from the
 * Kordis WebSocket stream.
 *
 * <p>Written only by the single WebSocket thread and read by HTTP request threads, so the
 * scalar fields are {@code volatile} for safe publication and {@code previousStops} is a
 * {@link CopyOnWriteArrayList} to allow concurrent iteration while new stops are appended.
 */
public class VehicleInfo {

    private volatile int carId;
    private volatile int lastStop;
    private volatile long changedAt;
    private volatile int delay;
    private transient volatile long lastSeen;
    private final List<StopDelay> previousStops = new CopyOnWriteArrayList<>();

    public VehicleInfo(int carId, int lastStop, long changedAt, int delay) {
        this.carId = carId;
        this.lastStop = lastStop;
        this.changedAt = changedAt;
        this.delay = delay;
        this.lastSeen = changedAt;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public int getLastStop() {
        return lastStop;
    }

    public void setLastStop(int lastStop) {
        this.lastStop = lastStop;
    }

    public long getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(long changedAt) {
        this.changedAt = changedAt;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public List<StopDelay> getPreviousStops() {
        return previousStops;
    }

    public String getJson(Gson gson) {
        var obj = new JsonObject();

        obj.addProperty("vehicleId", carId);
        obj.addProperty("delay", delay);
        obj.addProperty("lastStopId", lastStop);
        obj.addProperty("lastUpdated", lastSeen);
        obj.add("previousStops", gson.toJsonTree(previousStops, new TypeToken<List<StopDelay>>(){}.getType()));

        return obj.toString();
    }

}
