package me.miran.libreinfo.parsing.storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.miran.libreinfo.R;
import me.miran.libreinfo.exception.AppException;
import me.miran.libreinfo.exception.ErrorType;
import me.miran.libreinfo.util.AppInputStream;

public class StopMapper implements AppStorage {


    public static StopMapper parse(AppInputStream is) throws AppException {
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
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
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

    /**
     * @return Length of the internal stops. Can also be though of as the maximal internal stopID lowered by one
     */
    public int internalStopsLength() {
        return mappedToOriginal.length;
    }

}
