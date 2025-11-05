package me.miran.mhdstuff.parsing.types.departure;

import androidx.annotation.Nullable;

import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.types.Vehicle;
import me.miran.mhdstuff.util.request.soap.SoapSaneObject;
import com.google.gson.JsonObject;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Departures(String message, List<Departure> departures) {


    public static Departures parse(SoapSaneObject deps, @Nullable SoapSaneObject vehicles, int stopID, IdStorage storage, JsonObject delays) {
        if (deps == null) return null;

        String message = "";

        for (Object o : deps.getSoapSaneObject("InfoTextSet")) {
            message += o+"\n\n";
        }
        message = message.trim();

        Map<Integer, List<DepartureEntry>> departureMap = new HashMap<>();

        HashMap<Long, Vehicle> map = new HashMap<>();
        if (vehicles != null) {
            for (Object obj : vehicles) {
                Vehicle vehicle = Vehicle.parse(SoapSaneObject.parse((SoapObject) obj), storage);

                long key = ((long) vehicle.routeId() << 32) | ((long) vehicle.line().id());

                if (map.containsKey(key)) {
                    System.out.println("ALREADY HERE \n\t" + vehicle + "\n\t" + map.get(key));
                }
                map.put(key, vehicle);
            }
        }

        for (Object departureObj : deps.getSoapSaneObject("DeparturesL")) {
            DepartureEntry entry = DepartureEntry.parse(SoapSaneObject.parse((SoapObject) departureObj), map, storage);

            departureMap.computeIfAbsent(entry.postID(), k -> new ArrayList<>()).add(entry);
        }

        List<Departure> departures = new ArrayList<>();
        for (Map.Entry<Integer, List<DepartureEntry>> entry : departureMap.entrySet()) {
            String postName = storage.postStorage().getPost(stopID, entry.getKey()).name();
            departures.add(new Departure(entry.getKey(),postName, entry.getValue()));
        }
        departures.sort(Comparator.comparingInt(Departure::postID));

        return new Departures(message, departures);
    }

}
