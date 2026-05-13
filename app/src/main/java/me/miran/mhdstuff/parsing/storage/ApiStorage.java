package me.miran.mhdstuff.parsing.storage;

import me.miran.mhdstuff.util.Pair;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class ApiStorage {

    public static ApiStorage parse(DataInputStream is){
        try (is) {
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
            e.printStackTrace();
            return new ApiStorage(new int[0]);
        }

    }

    private final int[] map;

    private ApiStorage(int[] map) {
        this.map = map;
    }

    public Pair<Integer, Integer> getLineIdAndRoute(int tripId) {
        int value = map[tripId];
        if (value == 0xFFFF_FFFF) return new Pair<>(-1, -1);

        return new Pair<>(value>>16, value&0xFFFF);
    }

}
