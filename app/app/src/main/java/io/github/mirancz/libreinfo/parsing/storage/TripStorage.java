package io.github.mirancz.libreinfo.parsing.storage;

import android.util.SparseArray;

import io.github.mirancz.libreinfo.codec.TripsCodec;
import io.github.mirancz.libreinfo.codec.util.AppInputStream;
import io.github.mirancz.libreinfo.exception.AppException;
import io.github.mirancz.libreinfo.model.Trip;
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TripStorage implements AppStorage {

    public static TripStorage parse(AppInputStream is) throws AppException {
        try {
            var result = TripsCodec.read(is);

            return new TripStorage(result.getSecond(), result.getFirst());
        } catch (IOException e) {
            throw AppException.dataLoad(e);
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
            if (trip.getBlockId() == -1) continue;

            if (blockMap.get(trip.getBlockId()) == null) {
                blockMap.put(trip.getBlockId(), new ArrayList<>());
            }

            blockMap.get(trip.getBlockId()).add(trip);
        }
    }


    public String getTripHeadsign(Trip trip) {
        return headsignPool[trip.getHeadsignId()];
    }

    public String getHeadsignForTripList(List<Trip> trips, IdStorage storage) {
        trips.sort(Comparator.comparing(t -> storage.routeStopStorage().getRouteStop(t.getStartPos()).departure()));

        String headSign = "";
        boolean multiple = false;

        for (Trip neighbor : trips) {
            if (headSign.isEmpty()) {
                headSign = storage.tripStorage().getTripHeadsign(neighbor);
            } else {
                multiple = true;
                headSign = headSign + " (> "+storage.lineStorage().getAlias(neighbor.getLineId()).getDisplayName() + " "+storage.tripStorage().getTripHeadsign(neighbor);
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
