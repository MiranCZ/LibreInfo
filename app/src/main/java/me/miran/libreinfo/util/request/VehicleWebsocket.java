package me.miran.libreinfo.util.request;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.miran.libreinfo.util.AppLog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class VehicleWebsocket {

    private static final String SOCKET_URL = "wss://api.libre-info.com/stream";
    private static final OkHttpClient client = new OkHttpClient();
    private static final List<WebsocketListener> listeners = new ArrayList<>();
    private static final Map<Class<?>, List<WebsocketListener>> listenerMap = new HashMap<>();
    private static WebSocket socket;

    public static void subscribe(Class<?> clazz,WebsocketListener listener) {
        AppLog.d("new subscription for " + clazz.getSimpleName());
        if (socket == null) {
            createWebsocket();
        }
        listeners.add(listener);
        listenerMap.computeIfAbsent(clazz, k -> new ArrayList<>()).add(listener);
    }

    public static void unsubscribe(Class<?> clazz) {
        for (WebsocketListener listener : listenerMap.getOrDefault(clazz, List.of())) {
            AppLog.d("removing listener " + listener);
            listeners.remove(listener);
        }
        listenerMap.remove(clazz);

        if (listeners.isEmpty()) {
            if (socket != null) {
                AppLog.d("no listeners left, closing socket");
                socket.close(1000, "Client closing");
                socket = null;
            }
        }
    }

    private static void createWebsocket() {
        Request request = new Request.Builder()
                .url(SOCKET_URL)
                .build();

        socket = client.newWebSocket(request, new WebSocketListener() {
//            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                AppLog.d("Connected to WebSocket endpoint");
                // Example: send a filter message
                // webSocket.send("{\"where\": \"delay > 60\"}");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
//                buffer.append(text);
                // OkHttp delivers full text messages (no need to check "last" flag)
//                String fullMessage = buffer.toString();
//                System.out.println("Received full message: " + text);
//                buffer.setLength(0); // Reset buffer
                for (WebsocketListener listener : listeners) {
                    listener.onMessage(text);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                AppLog.d("Received binary message: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                AppLog.d("Connection closing: " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                AppLog.d("Connection closed: " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                AppLog.e("WebSocket failure", t);
            }
        });
    }



    @FunctionalInterface
    public interface WebsocketListener {
        void onMessage(String message);
    }


}
