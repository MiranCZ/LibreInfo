package me.miran.mhdstuff.parsing.storage;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StopMapper {


    public static StopMapper parse(DataInputStream is) {
        try (is) {
            int size = is.readInt();

            Map<Integer, Integer> map = new HashMap<>();
            int[] mappedToNormal = new int[size];

            for (int i = 0; i < size; i++) {
                int original = is.readInt();

                map.put(original, i);
                mappedToNormal[i] = original;
            }

            return new StopMapper(map, mappedToNormal);
        } catch (IOException e) {
            e.printStackTrace();
            return new StopMapper(Map.of(), new int[0]);
        }
    }

    private final Map<Integer, Integer> originalToMapped;
    private final int[] mappedToOriginal;


    private StopMapper(Map<Integer, Integer> originalToMapped, int[] mappedToOriginal) {
        this.originalToMapped = originalToMapped;
        this.mappedToOriginal = mappedToOriginal;
    }

    public int getMapped(int original) {
        return originalToMapped.getOrDefault(original, -1);
    }

    public int getOriginal(int mapped) {
        if (mapped < 0 || mapped >= mappedToOriginal.length) return -1;

        return mappedToOriginal[mapped];
    }

}
