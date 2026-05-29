package me.miran;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.miran.model.StopDelay;
import me.miran.model.VehicleInfo;
import spark.Request;
import spark.Response;

import java.util.LinkedHashMap;
import java.util.Map;

public class Endpoints {

    private static final String JSON = "application/json";

    private final VehicleQuery query;
    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public Endpoints(VehicleQuery query) {
        this.query = query;
    }

    public String handleVehicles(Request request, Response response) {
        response.type(JSON);
        return gson.toJson(query.getVehicles());
    }

    public String handleVehicleInfo(Request request, Response response) {
        final String notFound = error("Trip with the specified parameters was not found.");

        response.type(JSON);
        String lineIdStr = request.queryParams("lineid");
        String routeIdStr = request.queryParams("routeid");

        if (lineIdStr == null && routeIdStr == null) {
            return gson.toJson(query.getVehicleInfos());
        }
        if (lineIdStr == null) {
            return error("'lineid' cannot be null when 'routeid' is provided");
        }

        int lineId;
        try {
            lineId = Integer.parseInt(lineIdStr);
            if (routeIdStr != null) {
                int routeId = Integer.parseInt(routeIdStr);
                VehicleInfo trip = query.getTrip(lineId, routeId);
                return trip == null ? notFound : gson.toJson(trip);
            }
        } catch (NumberFormatException ignored) {
            return error("Invalid parameters");
        }

        Map<Integer, VehicleInfo> line = query.getLine(lineId);
        return line == null ? notFound : gson.toJson(line);
    }

    public String handleStopDelays(Request request, Response response) {
        response.type(JSON);
        String stopIdStr = request.queryParams("stopid");
        if (stopIdStr == null) {
            return error("Parameter 'stopid' needs to be supplied!");
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
                        if (stop.id() == stopId) {
                            result.computeIfAbsent(lineId, k -> new LinkedHashMap<>())
                                    .put(routeId, new StopDelayView(stop.delay(), info.getChangedAt()));
                            break;
                        }
                    }
                }));
        return gson.toJson(result);
    }

    public String handleRouteDelays(Request request, Response response) {
        response.type(JSON);
        Map<Integer, Map<Integer, RouteDelayView>> result = new LinkedHashMap<>();
        query.getVehicleInfos().forEach((lineId, lineMap) ->
                lineMap.forEach((routeId, info) ->
                        result.computeIfAbsent(lineId, k -> new LinkedHashMap<>())
                                .put(routeId, new RouteDelayView(
                                        info.getDelay(), info.getCarId(), info.getChangedAt()))));
        return gson.toJson(result);
    }

    private String error(String reason) {
        var json = new JsonObject();
        json.addProperty("error", reason);
        return json.toString();
    }

    private record StopDelayView(int delay, long changedAt) {
    }

    private record RouteDelayView(int delay, String carId, long changedAt) {
    }
}
