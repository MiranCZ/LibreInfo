package me.miran;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.miran.model.Vehicle;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * WebSocket transport for the Kordis ArcGIS GeoEvent stream.
 *
 * <p>Owns only the connection lifecycle (connect, reconnect, TLS) and the wire-format parsing.
 * Each parsed {@link Vehicle} is pushed to the supplied sink, keeping this class unaware of how
 * the state is stored or served.
 */
public class KordisStream {

    private static final Logger log = LoggerFactory.getLogger(KordisStream.class);

    private static final URI URL = URI.create(
            "wss://gis.brno.cz/geoevent/ws/services/stream_kordis_26/StreamServer/subscribe");

    /** Delay before attempting to reconnect after the connection drops (matches the Python 120s). */
    private static final long RECONNECT_DELAY_SECONDS = 120;

    private final Consumer<Vehicle> sink;

    private final ScheduledExecutorService reconnectExecutor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "kordis-reconnect");
                t.setDaemon(true);
                return t;
            });

    /** Guards against scheduling overlapping reconnect attempts. */
    private final AtomicBoolean reconnectScheduled = new AtomicBoolean(false);

    private volatile KordisClient client;

    public KordisStream(Consumer<Vehicle> sink) {
        this.sink = sink;
    }

    /** Opens the WebSocket connection. */
    public void start() {
        connect();
    }

    /** Closes the connection and stops reconnection attempts. */
    public void stop() {
        reconnectExecutor.shutdownNow();
        KordisClient c = client;
        if (c != null) {
            c.close();
        }
    }

    /** Parses one stream message into a {@link Vehicle}. Package-private for testing. */
    static Vehicle parse(String message) {
        JsonObject data = JsonParser.parseString(message).getAsJsonObject().getAsJsonObject("attributes");
        return new Vehicle(
                data.get("ID").getAsString(),
                data.get("LineID").getAsInt(),
                data.get("RouteID").getAsInt(),
                data.get("LastStopID").getAsInt(),
                data.get("Delay").getAsInt(),
                data);
    }

    private void connect() {
        try {
            KordisClient c = new KordisClient();
            // Required for wss:// — supply the default TLS socket factory.
            c.setSocketFactory(SSLContext.getDefault().getSocketFactory());
            client = c;
            c.connect();
        } catch (Exception e) {
            log.error("Failed to start WebSocket connection", e);
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (reconnectScheduled.compareAndSet(false, true)) {
            log.info("Reconnecting in {}s", RECONNECT_DELAY_SECONDS);
            reconnectExecutor.schedule(() -> {
                reconnectScheduled.set(false);
                connect();
            }, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }

    private final class KordisClient extends WebSocketClient {

        private KordisClient() {
            super(URL);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            log.info("WebSocket connected to {}", URL);
        }

        @Override
        public void onMessage(String message) {
            try {
                sink.accept(parse(message));
            } catch (Exception e) {
                log.warn("Failed to process message: {}", message, e);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            log.info("WebSocket closed (code={}, remote={}): {}", code, remote, reason);
            scheduleReconnect();
        }

        @Override
        public void onError(Exception e) {
            log.error("WebSocket error", e);
            // onClose follows onError for connection failures and drives the reconnect.
        }
    }
}
