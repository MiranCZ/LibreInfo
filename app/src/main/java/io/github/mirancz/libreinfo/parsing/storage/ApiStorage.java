package io.github.mirancz.libreinfo.parsing.storage;

import io.github.mirancz.libreinfo.R;
import io.github.mirancz.libreinfo.exception.AppException;
import io.github.mirancz.libreinfo.exception.ErrorType;
import io.github.mirancz.libreinfo.util.AppInputStream;
import io.github.mirancz.libreinfo.util.Pair;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ApiStorage implements AppStorage {

    public static ApiStorage parse(AppInputStream is) throws AppException {
        try {
            int size = is.readInt();

            int[] map = new int[size];
            Arrays.fill(map, 0xFFFF_FFFF);

            for (int i = 0; i < size; i++) {
                int tripId = is.readInt();

                int combined = is.readInt();

                map[tripId] = combined;
            }

            return new ApiStorage(map);
        } catch (IOException e) {
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
        }

    }

    private final int[] map;
    private Map<Integer, Integer> reverseMap;

    private ApiStorage(int[] map) {
        this.map = map;
    }

    public Pair<Integer, Integer> getLineIdAndRoute(int tripId) {
        int value = map[tripId];
        if (value == 0xFFFF_FFFF) return new Pair<>(-1, -1);

        return new Pair<>(value>>16, value&0xFFFF);
    }

    /**
     * Resolves a trip from the line/route pair the API works with.
     *
     * @return the trip ID, or -1 when no trip carries that pair
     */
    public int getTripId(int lineId, int routeId) {
        if (lineId < 0 || routeId < 0) return -1;

        return getReverseMap().getOrDefault((lineId<<16) | (routeId&0xFFFF), -1);
    }

    /**
     * Only the server departure boards need the inverse lookup, so the map is built on first use
     * instead of during parsing, which runs on every cold start.
     */
    private synchronized Map<Integer, Integer> getReverseMap() {
        if (reverseMap != null) return reverseMap;

        Map<Integer, Integer> reverse = new HashMap<>(map.length);
        for (int tripId = 0; tripId < map.length; tripId++) {
            int value = map[tripId];
            if (value == 0xFFFF_FFFF) continue;

            // a line/route pair is expected to be unique, but keep the first trip if it is not
            reverse.putIfAbsent(value, tripId);
        }

        reverseMap = reverse;
        return reverseMap;
    }

}
