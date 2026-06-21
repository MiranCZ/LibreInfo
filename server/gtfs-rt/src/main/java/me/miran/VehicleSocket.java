package me.miran;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.miran.model.Vehicle;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@WebSocket
public class VehicleSocket {

    private static final Logger log = LoggerFactory.getLogger(VehicleSocket.class);

    private static final int MAX_CONNECTIONS = 10_000;

    private final VehicleQuery store;
    private final Gson gson;

    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    private final ExecutorService broadcaster =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "vehicle-socket-broadcast");
                t.setDaemon(true);
                return t;
            });

    public VehicleSocket(VehicleQuery store, Gson gson) {
        this.store = store;
        this.gson = gson;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        if (sessions.size() >= MAX_CONNECTIONS) {
            log.info("Refusing client: at capacity ({} connections)", MAX_CONNECTIONS);
            session.close(StatusCode.TRY_AGAIN_LATER, "server at capacity");
            return;
        }

        sessions.add(session);
        log.info("Client connected ({} total)", sessions.size());
        send(session, snapshotJson(store.getVehicles(), gson));
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        drop(session);
        log.info("Client disconnected (code={}, {} remaining): {}", statusCode, sessions.size(), reason);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        drop(session);
        log.warn("WebSocket error, dropping client", error);
    }

    public void broadcast(Vehicle vehicle) {
        if (sessions.isEmpty()) {
            return;
        }
        String frame = updateJson(vehicle.raw(), gson);

        broadcaster.execute(() -> {
            for (Session session : sessions) {
                send(session, frame);
            }
        });
    }

    public void stop() {
        broadcaster.shutdownNow();
    }

    private void drop(Session session) {
        sessions.remove(session);
        session.close();
    }

    private void send(Session session, String text) {
        if (!session.isOpen()) {
            drop(session);
            return;
        }

        session.getRemote().sendString(text, new WriteCallback() {
            @Override
            public void writeSuccess() {
            }

            @Override
            public void writeFailed(Throwable x) {
                log.debug("Async write failed, dropping client", x);
                drop(session);
            }
        });
    }

    private static String snapshotJson(Collection<JsonObject> vehicles, Gson gson) {
        JsonArray array = new JsonArray();
        for (JsonObject vehicle : vehicles) {
            array.add(vehicle);
        }

        JsonObject frame = new JsonObject();

        frame.addProperty("type", "snapshot");
        frame.add("vehicles", array);
        return gson.toJson(frame);
    }

    private static String updateJson(JsonObject raw, Gson gson) {
        JsonObject frame = new JsonObject();

        frame.addProperty("type", "update");
        frame.add("vehicle", raw);

        return gson.toJson(frame);
    }
}
