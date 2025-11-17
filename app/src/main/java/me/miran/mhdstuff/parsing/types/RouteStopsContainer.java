package me.miran.mhdstuff.parsing.types;


/**
 * @param stops An array of packed ints in the form of {@code (routeStopId<<32 | minOffset)}
 */
public record RouteStopsContainer(short postId, short serviceId, Time startTime, long[] stops) {
}
