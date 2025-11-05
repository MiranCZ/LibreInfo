package com.example.mhdstuff.parsing.storage;

import android.util.SparseArray;

import com.example.mhdstuff.parsing.types.Trip;
import com.example.mhdstuff.util.IOUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TripStorage {

    public static TripStorage parse(DataInputStream is){
        try(is) {
            int size = is.readInt();

            String[] headsignPool = new String[size];

            for (int i = 0; i < size; i++) {
                int key = is.readInt();
                int byteSize = is.readInt();

                byte[] bytes = IOUtil.readNBytes(is, byteSize);

                String name = new String(bytes, StandardCharsets.UTF_8);

                headsignPool[key] = name;
            }

            int tripSize = is.readInt();
            Trip[] trips = new Trip[tripSize];

            for (int i = 0; i < tripSize; i++) {
                short serviceId = is.readShort();
                short lineId = is.readShort();
                int headsignId = is.readInt();
                short blockId = is.readShort();
                byte data = (byte) is.read();
                int startPos = is.readInt();
                byte length = (byte) is.read();

                trips[i] = new Trip(i, serviceId, lineId, headsignId, blockId,data == 1, startPos, length);
            }
            return new TripStorage(headsignPool, trips);
        } catch (IOException e) {
            e.printStackTrace();

            return new TripStorage(new String[0], new Trip[0]);
        }
    }

    private final String[] headsignPool;
    private final Trip[] trips;
    private final SparseArray<List<Trip>> blockMap;

    private TripStorage(String[] headsignPool, Trip[] trips) {
        this.headsignPool = headsignPool;
        this.trips = trips;

        this.blockMap = new SparseArray<>();

        for (Trip trip : trips) {
            if (trip.blockId() == -1) continue;

            if (blockMap.get(trip.blockId()) == null) {
                blockMap.put(trip.blockId(), new ArrayList<>());
            }

            blockMap.get(trip.blockId()).add(trip);
        }
    }


    public String getTripHeadsign(Trip trip) {
        return headsignPool[trip.headsignId()];
    }

    public String getHeadsignForTripList(List<Trip> trips, IdStorage storage) {
        trips.sort(Comparator.comparing(t -> storage.routeStopStorage().getRouteStop(t.startPos()).departure()));

        String headSign = "";
        boolean multiple = false;

        for (Trip neighbor : trips) {
            if (headSign.isEmpty()) {
                headSign = storage.tripStorage().getTripHeadsign(neighbor);
            } else {
                multiple = true;
                headSign = headSign + " (> "+storage.lineStorage().getAlias(neighbor.lineId()).lineDisplayName() + " "+storage.tripStorage().getTripHeadsign(neighbor);
            }
        }
        if (multiple) {
            headSign = headSign + ")";
        }

        return headSign;
    }

    public Trip[] getTrips() {
        return trips;
    }

    public List<Trip> getTripsForBlock(int blockId) {
        return blockMap.get(blockId, List.of());
    }

}
