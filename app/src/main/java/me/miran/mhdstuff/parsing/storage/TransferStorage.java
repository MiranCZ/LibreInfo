package me.miran.mhdstuff.parsing.storage;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TransferStorage {


    public static TransferStorage parse(DataInputStream is) {
        try(is) {
            int simpleTransfers = is.readInt();

            Map<Integer, Map<Integer, Short>> transfers = new HashMap<>();
            for (int i = 0; i < simpleTransfers; i++) {
                short fromStopId = is.readShort();
                short fromPostId = is.readShort();

                short toStopId = is.readShort();
                short toPostId = is.readShort();

                int from = ((int)fromStopId << 16) | (fromPostId & 0xFFFF);
                int to = ((int)toStopId << 16) | (toPostId & 0xFFFF);

                byte transferType = is.readByte();
                if (transferType != 2) {
                    throw new IllegalStateException();
                }

                short minTransferTime = is.readShort();

                transfers.computeIfAbsent(from, k -> new HashMap<>()).put(to, minTransferTime);
            }

            return new TransferStorage(transfers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Map<Integer, Map<Integer, Short>> transferTimes;


    private TransferStorage(Map<Integer, Map<Integer, Short>> transferTimes) {
        this.transferTimes = transferTimes;
    }

    public short getTransferTime(short fromStopId, short fromPostId, short toStopId, short toPostId) {
        int from = ((int)fromStopId << 16) | (fromPostId & 0xFFFF);
        int to = ((int)toStopId << 16) | (toPostId & 0xFFFF);

        Map<Integer, Short> res = transferTimes.get(from);
        if (res == null) return 0;

        return res.getOrDefault(to, (short)0);
    }


}
