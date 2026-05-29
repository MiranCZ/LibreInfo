package me.miran.model;

/**
 * A single observation of a vehicle's delay at a given stop, kept as history
 * inside {@link VehicleInfo#getPreviousStops()}.
 *
 * @param id    the stop id ({@code LastStopID} from the Kordis stream)
 * @param delay the delay in seconds at that stop ({@code Delay})
 */
public record StopDelay(int id, int delay) {
}
