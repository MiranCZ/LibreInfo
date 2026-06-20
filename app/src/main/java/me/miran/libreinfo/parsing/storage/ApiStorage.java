package me.miran.libreinfo.parsing.storage;

import me.miran.libreinfo.R;
import me.miran.libreinfo.exception.AppException;
import me.miran.libreinfo.exception.ErrorType;
import me.miran.libreinfo.parsing.storage.manager.StorageProvider;
import me.miran.libreinfo.util.AppInputStream;
import me.miran.libreinfo.util.Pair;

import java.io.IOException;
import java.util.Arrays;

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

    private ApiStorage(int[] map) {
        this.map = map;
    }

    public Pair<Integer, Integer> getLineIdAndRoute(int tripId) {
        int value = map[tripId];
        if (value == 0xFFFF_FFFF) return new Pair<>(-1, -1);

        return new Pair<>(value>>16, value&0xFFFF);
    }

}
