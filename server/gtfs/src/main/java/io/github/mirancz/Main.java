package io.github.mirancz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.mirancz.gtfs.export.writer.*;
import io.github.mirancz.gtfs.ids.*;
import io.github.mirancz.request.*;
import io.github.mirancz.request.transform.DiversionMapper;
import io.github.mirancz.request.transform.EventMapper;
import io.github.mirancz.update.Updater;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;


import static spark.Spark.*;

public class Main {
    public static final Path GTFS_ZIP_PATH = Path.of("/data/raw/gtfs.zip");

    public static final Path OSM_DIR = Path.of("/data/osm");

    public static final Path OUTPUT_DIR = Path.of("/public");

    public static final Path STATE_DIR = Path.of("/state");
    private static final Path MAP_DIR = STATE_DIR.resolve("maps");

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


    static void main() throws Exception {
        setupUpdater();

        setupSpark();
    }

    private static void setupUpdater() throws Exception {
        var time = Duration.ofHours(10).toMillis();

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Updater.update();
                } catch (Exception e) {
                    e.printStackTrace();
//                    throw new RuntimeException(e);
                }
            }
        }, time, time);

        try {
            Updater.update();
        } catch (Exception e) {
            e.printStackTrace();
//          throw new RuntimeException(e);
        }
    }

    private static void setupSpark() {
        ipAddress("0.0.0.0");
        port(8080);

        Gson gson = new GsonBuilder().create();

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
        get("/departures", endpoints::handleDepartures);

        get("/diversions", endpoints::handleDiversions);
        get("/events", endpoints::handleEvents);
        get("/posts", endpoints::handlePosts);

        get("/routePath", endpoints::routePath);
    }


    public static Path getMapFile(String name) {
        return MAP_DIR.resolve(name);
    }

    public static Path getMappedGtfsFile() {
        return OUTPUT_DIR.resolve("gtfs.zip");
    }

}
