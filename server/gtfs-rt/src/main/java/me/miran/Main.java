package me.miran;

import static spark.Spark.*;

public class Main {

    public static void main(String[] args) {
        ipAddress("0.0.0.0");
        port(8080);

        VehicleStore store = new VehicleStore();
        store.start();

        KordisStream stream = new KordisStream(store::record);
        stream.start();

        Endpoints endpoints = new Endpoints(store);

        get("/vehicles", endpoints::handleVehicles);
        get("/vehicleinfo", endpoints::handleVehicleInfo);
        get("/stopdelays", endpoints::handleStopDelays);
        get("/routedelays", endpoints::handleRouteDelays);
    }

}
