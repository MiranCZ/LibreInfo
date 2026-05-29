package me.miran.model;

import com.google.gson.JsonObject;

/**
 * A single vehicle observation parsed from the Kordis stream.
 *
 * <p>The typed fields drive the trip-tracking logic in the store without depending on the wire
 * format. {@code raw} carries the original {@code attributes} object verbatim so the
 * {@code /vehicles} endpoint can mirror the WebSocket feed exactly.
 *
 * @param id       vehicle identifier ({@code ID})
 * @param lineId   line identifier ({@code LineID})
 * @param routeId  route/trip identifier ({@code RouteID})
 * @param lastStop the stop the vehicle last reported at ({@code LastStopID})
 * @param delay    current delay in seconds ({@code Delay})
 * @param raw      the original {@code attributes} payload, as received
 */
public record Vehicle(String id, int lineId, int routeId, int lastStop, int delay, JsonObject raw) {
}
