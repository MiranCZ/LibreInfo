package me.miran.model;

/**
 * A single vehicle observation parsed from the Kordis stream.
 *
 * <p>This is the domain representation handed to the store; it intentionally carries no
 * dependency on the wire format (Gson) so the store and HTTP layer stay decoupled from how
 * the data arrives.
 *
 * @param id       vehicle identifier ({@code ID})
 * @param lineId   line identifier ({@code LineID})
 * @param routeId  route/trip identifier ({@code RouteID})
 * @param lastStop the stop the vehicle last reported at ({@code LastStopID})
 * @param delay    current delay in seconds ({@code Delay})
 */
public record Vehicle(String id, int lineId, int routeId, int lastStop, int delay) {
}
