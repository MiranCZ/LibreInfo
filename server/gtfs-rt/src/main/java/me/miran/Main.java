package me.miran;

import static spark.Spark.*;

public class Main {

    public static void main(String[] args) {
        ipAddress("0.0.0.0");
        port(8080);

        get("/vehicles", Endpoints::handleVehicles);
        get("/vehicleinfo", Endpoints::handleVehicleInfo);
        get("/stopdelays", Endpoints::handleStopDelays);
        get("/routedelays", Endpoints::handleRouteDelays);
    }

}


