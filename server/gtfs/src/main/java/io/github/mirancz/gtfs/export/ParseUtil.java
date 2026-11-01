package io.github.mirancz.gtfs.export;

public class ParseUtil {

    public static StopInfo parseStop(String stopUID) {
        if (!stopUID.startsWith("U")) {
            throw new IllegalStateException("Invalid stop UID: "+stopUID);
        }

        // format is U{stopId}Z{postId} or U{stopId}N{postId}

        int zInd = stopUID.indexOf("Z");
        int nInd = stopUID.indexOf("N");
        int ind = Math.max(zInd, nInd);

        if (ind == -1) throw new IllegalArgumentException("Invalid stop UID: " + stopUID);

        int stopId = Integer.parseInt(stopUID.substring(1, ind));
        int postId = Integer.parseInt(stopUID.substring(ind + 1));

        // the gtfs is rewritten (GtfsRewriter) before parsing, so the stop number is already mapped
        if (stopId > Short.MAX_VALUE || postId > Short.MAX_VALUE) {
            throw new IllegalStateException();
        }

        StopInfo.PostType type;
        if (zInd != -1) type = StopInfo.PostType.POST;
        else type = StopInfo.PostType.PLATFORM;

        return new StopInfo((short) stopId, (short) postId, type);
    }

    public static int parseRoute(String routeId) {
        if (!routeId.startsWith("L")) {
            return -1;
        }
        routeId = routeId.substring(1);
        int zInd = routeId.indexOf("D");
        if (zInd == -1) return -1;

        return Integer.parseInt(routeId.substring(0, zInd));
    }


}
