package me.miran;

import com.google.gson.Gson;
import me.miran.ids.ApiVehicle;
import me.miran.model.StopDelay;
import me.miran.model.Vehicle;
import me.miran.model.VehicleInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class VehicleStore {

    private static final long TRIP_MAX_AGE_MS = TimeUnit.HOURS.toMillis(5);

    private final Gson gson;
    private final VehicleSocket socket;
    private final AtomicReference<Map<Integer, Vehicle>> vehicleMap = new AtomicReference<>(new HashMap<>());
    private final AtomicReference<String> vehiclesJson = new AtomicReference<>("{}");
    private final Map<Integer, Map<Integer, VehicleInfo>> vehicleInfos = new ConcurrentHashMap<>();

    public VehicleStore(Gson gson, VehicleSocket socket) {
        this.gson = gson;
        this.socket = socket;
    }


    public synchronized void onData(String data) {
        VehiclesResponse vehiclesResponse = gson.fromJson(data, VehiclesResponse.class);
        var vehicles = vehiclesResponse.vehicles;

        if (vehicles == null) return;

        long now = System.currentTimeMillis();

        Map<Integer, Vehicle> newMap = new ConcurrentHashMap<>();
        var prevMap = vehicleMap.get();

        for (ApiVehicle apiVehicle : vehicles) {
            try {
                Vehicle vehicle = apiVehicle.map();

                processVehicle(prevMap, newMap, vehicle, now);
                recordVehicleInfo(vehicle, now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        vehicleInfos.values().removeIf(lineMap -> {
            lineMap.values().removeIf(info -> now - info.getLastSeen() >= TRIP_MAX_AGE_MS);
            return lineMap.isEmpty();
        });

        vehicleMap.set(newMap);
        vehiclesJson.set(gson.toJson(newMap.values()));
    }

    private void processVehicle(Map<Integer, Vehicle> vehicleMap, Map<Integer, Vehicle> newMap, Vehicle vehicle, long now) {
        int id = vehicle.getId();

        newMap.put(id, vehicle);

        boolean broadcast = false;
        if (!vehicleMap.containsKey(id)) {
            broadcast = true;
        } else {
            Vehicle prevVehicle = vehicleMap.get(id);

            if (!prevVehicle.equals(vehicle)) {
                broadcast = true;
            }
        }

        if (broadcast) {
            socket.broadcast(vehicle);
        }
    }

    private void recordVehicleInfo(Vehicle v, long now) {
        var lineMap = vehicleInfos.computeIfAbsent(v.getLineId(), k -> new ConcurrentHashMap<>());

        VehicleInfo info = lineMap.get(v.getRouteId());


        if (info == null) {
            info = new VehicleInfo(v.getId(), v.getLastStopId(), now, v.getDelay());
            info.getPreviousStops().add(new StopDelay(v.getLastStopId(), v.getDelay()));
            lineMap.put(v.getRouteId(), info);
        } else if (info.getLastStop() != v.getLastStopId()) {
            info.setCarId(v.getId());
            info.setLastStop(v.getLastStopId());
            info.setChangedAt(now);
            info.getPreviousStops().add(new StopDelay(v.getLastStopId(), v.getDelay()));
        }
        // The latest delay and contact time are always recorded, even when the vehicle hasn't moved stops.
        info.setDelay(v.getDelay());
        info.setLastSeen(now);
    }

    public String getVehiclesJson() {
        return vehiclesJson.get();
    }

    public Map<Integer, Map<Integer, VehicleInfo>> getVehicleInfos() {
        return Collections.unmodifiableMap(vehicleInfos);
    }

    public VehicleInfo getTrip(int lineId, int routeId) {
        Map<Integer, VehicleInfo> lineMap = vehicleInfos.get(lineId);
        return lineMap == null ? null : lineMap.get(routeId);
    }

    private record VehiclesResponse(List<ApiVehicle> vehicles) {
    }

}
