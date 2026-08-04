package me.miran;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.miran.poll.Poller;
import me.miran.request.RequestHelper;
import me.miran.transform.DiversionMapper;
import me.miran.transform.EventMapper;

import java.time.Duration;

import static spark.Spark.*;

public class Main {

    public static Poller<String> diversionPoller = new Poller<>(
            "diversions",
            () -> "{\"diversions\":" + new Gson().toJson(DiversionMapper.mapDiversions(RequestHelper.getDiversions())) + "}",
            Duration.ofMinutes(5)
    );

    public static Poller<String> eventPoller = new Poller<>(
            "events",
            () -> "{\"events\":" + new Gson().toJson(EventMapper.mapEvents(RequestHelper.getEvents())) + "}",
            Duration.ofSeconds(45)
    );

    public static void main(String[] args) {
        ipAddress("0.0.0.0");
        port(8080);

        Gson gson = new GsonBuilder()
                .create();


        diversionPoller.start();
        eventPoller.start();

        VehicleSocket socket = new VehicleSocket(gson);
        webSocket("/stream", socket);

        var stream = new VehicleStore(gson, socket);

        var vehiclesPoller = new Poller<>("vehicles", RequestHelper::getVehicles, stream::onData, Duration.ofSeconds(5));
        vehiclesPoller.start();

        Endpoints endpoints = new Endpoints(stream, gson);

        get("/vehicles", endpoints::handleVehicles);
        get("/vehicleInfo", endpoints::handleVehicleInfo);
        get("/stopDelays", endpoints::handleStopDelays);
        get("/routeDelays", endpoints::handleRouteDelays);

        get("/diversions", endpoints::handleDiversions);
        get("/events", endpoints::handleEvents);
        get("/posts", endpoints::handlePosts);
    }

}
