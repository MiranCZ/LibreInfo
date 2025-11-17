package me.miran.mhdstuff.parsing.storage;

import java.io.DataInputStream;
import java.io.IOException;

import me.miran.mhdstuff.parsing.types.RouteStopsContainer;
import me.miran.mhdstuff.parsing.types.Time;

public class StopRouteContainerStorage {

    public static StopRouteContainerStorage parse(DataInputStream is) {
        try(is) {
            RouteStopsContainer[][] stuff;

            int size = is.readInt();
            stuff = new RouteStopsContainer[size][];

            for (int i = 0; i < size; i++) {
                int groupSize = is.readInt();
                stuff[i] = new RouteStopsContainer[groupSize];

                for (int j = 0; j < groupSize; j++) {
                    short stopId = is.readShort();
                    short postId = is.readShort();
                    short serviceId = is.readShort();
                    var startTime = parseTime(is);

                    int stopsLen = is.readShort();
                    long[] stops = new long[stopsLen];

                    for (int k = 0; k < stopsLen; k++) {
                        stops[k] = is.readLong();
                    }

                    RouteStopsContainer container = new RouteStopsContainer(postId, serviceId, startTime, stops);

                    stuff[i][j] = container;
                }

            }

            return new StopRouteContainerStorage(stuff);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static Time parseTime(DataInputStream is) throws IOException {
        int hours = is.read();
        int minutes = is.read();

        int result = hours*60 + minutes;


//        if (result > Short.MAX_VALUE) throw new IllegalStateException();
//        return (short) result;

       return new Time(hours, minutes);
    }

    public final RouteStopsContainer[][] stopIdToRoute;

    private StopRouteContainerStorage(RouteStopsContainer[][] stopIdToRoute) {
        this.stopIdToRoute = stopIdToRoute;
    }

}
