package com.example.mhdstuff.parsing.storage;

import com.example.mhdstuff.parsing.types.Trip;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TripStorage {

    public static TripStorage parse(DataInputStream is){
        try(is) {
            int size = is.readInt();

            String[] headsignPool = new String[size];

            for (int i = 0; i < size; i++) {
                int key = is.readInt();
                int byteSize = is.readInt();

                byte[] bytes = is.readNBytes(byteSize);

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
//                int startPos = is.readInt();
//                byte length = (byte) is.read();

                trips[i] = new Trip(serviceId, lineId, headsignId, -1, (byte) 0);
            }
            return new TripStorage(headsignPool, trips);
        } catch (IOException e) {
            e.printStackTrace();

            return new TripStorage(new String[0], new Trip[0]);
        }
    }

    private final String[] headsignPool;
    private final Trip[] trips;

    private TripStorage(String[] headsignPool, Trip[] trips) {
        this.headsignPool = headsignPool;
        this.trips = trips;
    }


    public String getTripHeadsign(Trip trip) {
        return headsignPool[trip.headsignId()];
    }

    public Trip[] getTrips() {
        return trips;
    }

}
