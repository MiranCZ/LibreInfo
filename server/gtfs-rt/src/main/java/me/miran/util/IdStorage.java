package me.miran.util;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * The id tables written by gtfs-parser into the state dir: for every id of ours, the id the upstream
 * API uses for it. Loaded at startup and whenever gtfs-parser has written new ones ({@link #reload()}).
 */
public class IdStorage {

    private static final Path STATE_DIR = Path.of("/state");

    public static final MappingIndexer STOP = new MappingIndexer(STATE_DIR.resolve("stop_maps").toFile());
    public static final MappingIndexer SERVICE = new MappingIndexer(STATE_DIR.resolve("service_maps").toFile());

    private IdStorage() {
    }

    /**
     * Re-reads every table. A table that cannot be read keeps its previous contents and the
     * failure is thrown after the others have been reloaded.
     */
    public static synchronized void reload() {
        RuntimeException failure = null;

        for (MappingIndexer indexer : new MappingIndexer[]{STOP, SERVICE}) {
            try {
                indexer.reload();
            } catch (RuntimeException e) {
                if (failure == null) failure = e;
                else failure.addSuppressed(e);
            }
        }

        if (failure != null) throw failure;
    }

    public static class MappingIndexer {

        private record Table(Map<Integer, Integer> idToIndex, int[] indexToId) {
        }

        private static final Table EMPTY = new Table(Map.of(), new int[0]);

        private final File file;
        private volatile Table table;

        private MappingIndexer(File file) {
            this.file = file;
            this.table = load(file);
        }

        /** Re-reads the file; the old contents stay in use if that fails. */
        void reload() {
            this.table = load(file);
        }

        public int size() {
            return table.indexToId.length;
        }

        /** Upstream id -> our id. */
        public Mapped<Integer> getId(Integer original) {
            if (original == null) return new Mapped.Present<>(null);

            return Mapped.of(table.idToIndex.get(original));
        }

        /** Our id -> upstream id, or null when we have no such id. */
        public Integer getOriginal(int id) {
            int[] indexToId = table.indexToId;
            if (id < 0 || id >= indexToId.length) return null;

            return indexToId[id];
        }

        /** Layout: the count, then the original (upstream) id of every index, in index order. */
        private static Table load(File file) {
            if (!file.exists()) return EMPTY;

            try (DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

                int size = is.readInt();

                Map<Integer, Integer> map = new HashMap<>();
                int[] originals = new int[size];

                for (int i = 0; i < size; i++) {
                    originals[i] = is.readInt();
                    map.put(originals[i], i);
                }

                return new Table(map, originals);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

    }

}
