package me.miran.util;

import me.miran.Main;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class IdStorage {

    public static final MappingIndexer STOP;

    static {
        STOP = load(Main.getStopMapsFile());
    }

    private static MappingIndexer load(File file) {
        if (!file.exists()) return new MappingIndexer(new HashMap<>());

        try (DataInputStream is = new DataInputStream(new FileInputStream(file))) {

            int size = is.readInt();

            Map<Integer, Integer> map = new HashMap<>();

            for (int i = 0; i < size; i++) {
                map.put(is.readInt(), i);
            }

            return new MappingIndexer(map);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @FunctionalInterface
    public interface Indexer {
        int getId(int original);
    }

    public static class MappingIndexer implements Indexer {

        private final Map<Integer, Integer> idToIndex;
        private int ID;

        private MappingIndexer(Map<Integer, Integer> initial) {
            this.idToIndex = initial;
            this.ID = initial.size();
        }

        public int getId(int original) {
            return idToIndex.computeIfAbsent(original, k -> ID++);
        }

        /**
         * Looks up an already-assigned id, returning -1 when there is none. Unlike
         * {@link #getId(int)} this never assigns, so it is safe to call after the
         * mapping has been written.
         */
        public int getExistingId(int original) {
            return idToIndex.getOrDefault(original, -1);
        }

        /**
         * Writes the table: the count, then the original id of every index, in index order.
         * This is the format {@link #load} (and gtfs-rt) reads back.
         */
        public void write(CheckedOutputStream os) throws IOException {
            os.writeInt(idToIndex.size());

            int[] ids = new int[idToIndex.size()];

            for (Map.Entry<Integer, Integer> entry : idToIndex.entrySet()) {
                ids[entry.getValue()] = entry.getKey();
            }

            for (int id : ids) {
                os.writeInt(id);
            }
        }

        /**
         * Same layout as {@link #write}, but every index maps to itself. Clients only ever see
         * the mapped ids now, so they get a no-op mapping (kept for backwards compatibility).
         */
        public void writeNoop(CheckedOutputStream os) throws IOException {
            os.writeInt(idToIndex.size());

            for (int i = 0; i < idToIndex.size(); i++) {
                os.writeInt(i);
            }
        }

    }

}
