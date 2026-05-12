package me.miran.mhdstuff.parsing.storage;

import me.miran.mhdstuff.parsing.types.RouteStop;
import me.miran.mhdstuff.parsing.types.Time;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RouteStopStorage {


    public static RouteStopStorage parse(DataInputStream is, RandomAccessFile routeStops, StopMapper mapper) {
        try(is) {
            int[][] stopIdToRoute = new int[mapper.internalStopsLength()][];

            int size = is.readInt();

            for (int i = 0; i < size; i++) {
                short stopId = is.readShort();

                int arrSize = is.readInt();
                int[] route = new int[arrSize];

                for (int j = 0; j < arrSize; j++) {
                    route[j] = is.readInt();
                }
                stopIdToRoute[stopId] = route;
            }

            return new RouteStopStorage(stopIdToRoute, routeStops);
        } catch (IOException e) {
            e.printStackTrace();
            return new RouteStopStorage(new int[0][], routeStops);
        }
    }


    private static final int ROUTE_STOP_SIZE_BYTES = Short.BYTES + Integer.BYTES + 2 * Short.BYTES + 4 * Byte.BYTES;
    private final int[][] stopIdToRouteStops;
    private final byte[] buffer = new byte[ROUTE_STOP_SIZE_BYTES];
    private final RandomAccessFile routeStops;


    private RouteStopStorage(int[][] stopIdToRoute, RandomAccessFile routeStops) {
        this.stopIdToRouteStops = stopIdToRoute;
        this.routeStops = routeStops;
    }

    public int[] getRouteStops(short stopId) {
        return stopIdToRouteStops[stopId];
    }

    public RouteStop[] getRouteStopsParsed(int stopId) {
        try {
            return getRouteStopsParsedInternal((short) stopId);
        } catch (IOException e) {
            e.printStackTrace();
            return new RouteStop[0];
        }
    }

    public RouteStop[] getRouteStopsFromSegmentParsed(int start, int length) {
        try {
            return getRouteStopsFromSegmentParsedInternal(start, length);
        } catch (IOException e) {
            e.printStackTrace();
            return new RouteStop[0];
        }
    }

    public RouteStop getRouteStop(int id) {
        try {
            return parseStop(id);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private RouteStop[] getRouteStopsFromSegmentParsedInternal(int start, int length) throws IOException {
        RouteStop[] results = new RouteStop[length];

        for (int i = 0; i < length; i++) {
            int routeId = start+i;

            RouteStop stop = parseStop(routeId);
            results[i] = stop;
        }

        return results;
    }


    private RouteStop parseStop(int routeId) throws IOException {
        long pos = (long) (ROUTE_STOP_SIZE_BYTES) * routeId;
        routeStops.seek(pos);

        int len = routeStops.read(buffer);
        if (len != buffer.length) {
            throw new IllegalStateException();
        }

        int bufferPos = 0;
        short stopId = readShort(buffer, bufferPos);
        bufferPos += 2;

        int tripId = readInt(buffer, bufferPos);
        bufferPos += 4;

        short postId = readShort(buffer, bufferPos);
        bufferPos += 2;

        short sequence = readShort(buffer, bufferPos);
        bufferPos += 2;

        Time arrival = new Time(buffer[bufferPos++], buffer[bufferPos++]);
        Time departure = new Time(buffer[bufferPos++], buffer[bufferPos++]);

        return new RouteStop(stopId, tripId, postId, sequence, arrival, departure);
    }

    private RouteStop[] getRouteStopsParsedInternal(short stopId) throws IOException {
        int[] routes = getRouteStops(stopId);
        if (routes == null || routes.length == 0) return new RouteStop[0];

        RouteStop[] results = new RouteStop[routes.length];

        // bulk-reading like this saves a LOT of time
        for (int i = 0, routesLength = routes.length; i < routesLength; i++) {
            int routeId = routes[i];
            long pos = (long) (ROUTE_STOP_SIZE_BYTES) * routeId;
            routeStops.seek(pos);

            int len = routeStops.read(buffer);
            if (len != buffer.length) {
                throw new IllegalStateException();
            }

            int bufferPos = 0;
            short sid = readShort(buffer, bufferPos);
            bufferPos += 2;

            int tripId = readInt(buffer, bufferPos);
            bufferPos += 4;

            short postId = readShort(buffer, bufferPos);
            bufferPos += 2;

            short sequence = readShort(buffer, bufferPos);
            bufferPos += 2;

            Time arrival = new Time(buffer[bufferPos++], buffer[bufferPos++]);
            Time departure = new Time(buffer[bufferPos++], buffer[bufferPos++]);

            results[i] = new RouteStop(sid, tripId, postId, sequence, arrival, departure);
        }
        return results;
    }

    private short readShort(byte[] buffer, int pos) {
        int ch1 = buffer[pos++] & 0xFF;
        int ch2 = buffer[pos++] & 0xFF;
        return (short)((ch1 << 8) | (ch2));
    }

    private int readInt(byte[] buffer, int pos) {
        int ch1 = (int) buffer[pos++] & 0xFF;
        int ch2 = (int) buffer[pos++] & 0xFF;
        int ch3 = (int) buffer[pos++] & 0xFF;
        int ch4 = (int) buffer[pos++] & 0xFF;

        return ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4));

    }

}
