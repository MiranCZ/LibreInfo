package me.miran;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static spark.Spark.*;

public class Main {

    public static void main(String[] args) {
        ipAddress("0.0.0.0");
        port(8080);

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        VehicleStore store = new VehicleStore();
        store.start();

        VehicleSocket socket = new VehicleSocket(store, gson);
        webSocket("/stream", socket);

        KordisStream stream = new KordisStream(v -> {
            store.record(v);
            socket.broadcast(v);
        });
        stream.start();

        Endpoints endpoints = new Endpoints(store, gson);

        get("/vehicles", endpoints::handleVehicles);
        get("/vehicleinfo", endpoints::handleVehicleInfo);
        get("/stopdelays", endpoints::handleStopDelays);
        get("/routedelays", endpoints::handleRouteDelays);
    }

}
