package me.miran;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import spark.Request;
import spark.Response;
import spark.Route;


public class Endpoints {

    private Endpoints() {
        // helper class
    }

    public static String handleVehicles(Request request, Response response) {
        return error("Not implemented!");
    }

    public static String handleVehicleInfo(Request request, Response response) {
        String lineIdStr = request.queryParams("lineid");
        String routeIdStr = request.queryParams("routeid");

        if (lineIdStr == null && routeIdStr != null) {
            // error
        }


        return error("Not implemented!");
    }

    public static String handleStopDelays(Request request, Response response) {
        String stopIdStr = request.queryParams("stopid");
        if (stopIdStr == null) {
            return error("Parameter 'stopid' needs to be supplied!");
        }

        int stopId;
        try {
            stopId = Integer.parseInt(stopIdStr);
        } catch (NumberFormatException ignored) {
            return error("Value '"+stopIdStr+"' could not be parsed as a number!");
        }

        return error("Not implemented!");
    }

    public static String handleRouteDelays(Request request, Response response) {
        return error("Not implemented!");
    }

    private static String error(String reason) {
        var json = new JsonObject();
        json.addProperty("error", reason);
        return json.toString();
    }


}
