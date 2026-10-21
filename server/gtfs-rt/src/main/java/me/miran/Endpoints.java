package me.miran;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.miran.model.StopDelay;
import me.miran.model.VehicleInfo;
import me.miran.request.RequestHelper;
import me.miran.util.IdStorage;
import spark.Request;
import spark.Response;

import java.util.LinkedHashMap;
import java.util.Map;

public class Endpoints {

    private static final String JSON = "application/json";

    private final VehicleStore query;
    private final Gson gson;

    public Endpoints(VehicleStore query, Gson gson) {
        this.query = query;
        this.gson = gson;
    }

    public String handleVehicles(Request request, Response response) {
        response.type(JSON);
        return "{\"vehicles\":" + query.getVehiclesJson() + "}";
    }

    public String handleVehicleInfo(Request request, Response response) {
        final String notFound = error("Trip with the specified parameters was not found.");

        response.type(JSON);
        String lineIdStr = request.queryParams("lineId");
        String routeIdStr = request.queryParams("routeId");

        if (lineIdStr == null || routeIdStr == null) {
            return error("'lineId' and 'routeId' parameters are required");
        }

        int lineId;
        int routeId;

        try {
            lineId = Integer.parseInt(lineIdStr);
            routeId = Integer.parseInt(routeIdStr);
        } catch (NumberFormatException ignored) {
            return error("Invalid parameters");
        }

        VehicleInfo trip = query.getTrip(lineId, routeId);
        return trip == null ? notFound : trip.getJson(gson);
    }

    public String handleStopDelays(Request request, Response response) {
        response.type(JSON);
        String stopIdStr = request.queryParams("stopId");
        if (stopIdStr == null) {
            return error("Parameter 'stopId' needs to be supplied!");
        }

        int stopId;
        try {
            stopId = Integer.parseInt(stopIdStr);
        } catch (NumberFormatException ignored) {
            return error("Value '" + stopIdStr + "' could not be parsed as a number!");
        }

        Map<Integer, Map<Integer, StopDelayView>> result = new LinkedHashMap<>();
        query.getVehicleInfos().forEach((lineId, lineMap) ->
                lineMap.forEach((routeId, info) -> {
                    for (StopDelay stop : info.getPreviousStops()) {
                        if (stop.getId() == stopId) {
                            result.computeIfAbsent(lineId, k -> new LinkedHashMap<>())
                                    .put(routeId, new StopDelayView(stop.getDelay(), info.getChangedAt()));
                            break;
                        }
                    }
                }));
        return "{\"stopDelays\":" + gson.toJson(result) + "}";
    }

    public String handleRouteDelays(Request request, Response response) {
        response.type(JSON);
        Map<Integer, Map<Integer, RouteDelayView>> result = new LinkedHashMap<>();
        query.getVehicleInfos().forEach((lineId, lineMap) ->
                lineMap.forEach((routeId, info) ->
                        result.computeIfAbsent(lineId, k -> new LinkedHashMap<>())
                                .put(routeId, new RouteDelayView(
                                        info.getDelay(), info.getCarId(), info.getChangedAt()))));

        return "{\"routeDelays\":" + gson.toJson(result) + "}";
    }


    public String handleDiversions(Request request, Response response) {
        response.type(JSON);

        var value = Main.diversionPoller.latestValue();
        if (value == null) {
            return "{}";
        }
        return value;
    }


    public String handleEvents(Request request, Response response) {
        response.type(JSON);

        var value = Main.eventPoller.latestValue();
        if (value == null) {
            return "{}";
        }
        return value;
    }

    public String handlePosts(Request request, Response response) {
        response.type(JSON);

        try {
            return gson.toJson(RequestHelper.getPosts());
        } catch (RuntimeException e) {
            // Unlike the other handlers, failures must not be a 200 — the caller bakes this
            // response into the static bundle and cannot tell an error body from real data.
            response.status(502);
            return error("Upstream posts request failed: " + e.getMessage());
        }
    }

    public String routePath(Request request, Response response) {
        response.type(JSON);

        String serviceId = request.queryParams("serviceId");
        String lineId = request.queryParams("lineId");
        String routeId = request.queryParams("routeId");
        try {
            return RequestHelper.getRoutePath(serviceId, lineId, routeId);
        } catch (RuntimeException e) {
            // Unlike the other handlers, failures must not be a 200 — the caller bakes this
            // response into the static bundle and cannot tell an error body from real data.
            response.status(502);
            return error("Upstream posts request failed: " + e.getMessage());
        }
    }

    public String handleDepartures(Request request, Response response) {
        response.type(JSON);

        String stopIdStr = request.queryParams("stopId");
        if (stopIdStr == null || stopIdStr.isBlank()) {
            return error("Parameter 'stopId' needs to be supplied!");
        }

        int stopId;
        try {
            stopId = Integer.parseInt(stopIdStr.strip());
        } catch (NumberFormatException ignored) {
            return error("Value '" + stopIdStr + "' could not be parsed as a number!");
        }

        try {
            return RequestHelper.getDepartures(stopId);
        } catch (IllegalArgumentException e) {
            response.status(404);
            return error(e.getMessage());
        } catch (RuntimeException e) {
            response.status(502);
            return error("Upstream departures request failed: " + e.getMessage());
        }
    }

    public String handleReload(Request request, Response response) {
        response.type(JSON);

        try {
            IdStorage.reload();
        } catch (RuntimeException e) {
            e.printStackTrace();
            response.status(500);
            return error("Reloading the id tables failed: " + e.getMessage());
        }

        var json = new JsonObject();
        json.addProperty("stops", IdStorage.STOP.size());
        json.addProperty("services", IdStorage.SERVICE.size());
        return json.toString();
    }

    private String error(String reason) {
        var json = new JsonObject();
        json.addProperty("error", reason);
        return json.toString();
    }

    private record StopDelayView(int delay, long lastModifiedAt) {
    }

    private record RouteDelayView(int delay, int vehicleId, long lastModifiedAt) {
    }
}
