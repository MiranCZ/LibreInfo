package com.example.mhdstuff.parsing.storage;

import com.example.mhdstuff.util.Pair;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiStorage {

    public static ApiStorage parse(DataInputStream is){
        try (is) {
            int size = is.readInt();

            Map<Integer, Integer> map = new HashMap<>(size);

            for (int i = 0; i < size; i++) {
                int tripId = is.readInt();

                int combined = is.readInt();

                map.put(tripId, combined);
            }

            return new ApiStorage(map);
        } catch (IOException e) {
            e.printStackTrace();
            return new ApiStorage(Map.of());
        }

    }

    private final Map<Integer, Integer> map;

    private ApiStorage(Map<Integer, Integer> map) {
        this.map = map;
    }

    public Pair<Integer, Integer> getLineIdAndRoute(int tripId) {
        Integer value = map.get(tripId);
        if (value == null) return new Pair<>(-1, -1);

        return new Pair<>(value>>16, value&0xFFFF);
    }

}
