package io.github.mirancz.gtfs.remap;

import io.github.mirancz.Main;
import io.github.mirancz.request.util.Mapped;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Mapper {

    private static final Map<IdType, Mapper> mappers = new HashMap<>();

    // TODO error handling
    public static void savePersistentMaps() {
        for (Map.Entry<IdType, Mapper> e : mappers.entrySet()) {
            if (e.getKey().persist) {

                var file = Main.getMapFile(e.getKey().name());

                try {
                    Files.createDirectories(file.getParent());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    continue;
                }

                try(var os = new DataOutputStream(Files.newOutputStream(file))) {
                    e.getValue().writeMaps(os);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static Mapped<Integer> getMapped(IdType type, Integer id) {
        if (id == null) return new Mapped.Present<>(null);

        var mapper = mappers.get(type);
        if (mapper == null) {
            return Mapped.of(null);
        }

        return mapper.tryMap(id);
    }

    public static Object map(IdType type, String id) {
        if (id.isBlank()) {
            return null;
        }

        if (type == IdType.STOP) {
            return mapStop(id);
        }

        return map(type, Integer.parseInt(id.trim()));
    }

    private static String mapStop(String stopID) {
        if (!stopID.startsWith("U")) {
            throw new IllegalStateException("Invalid stop UID: " + stopID);
        }

        int ind = Math.max(stopID.indexOf('Z'), stopID.indexOf('N'));
        if (ind == -1) throw new IllegalArgumentException("Invalid stop UID: " + stopID);

        int stopId = Integer.parseInt(stopID.substring(1, ind));

        return "U" + map(IdType.STOP, stopId) + stopID.substring(ind);
    }

    public static int map(IdType type, int id) {
        var mapper = mappers.get(type);
        if (mapper == null) {
            Mapper n = createMapper(type);

            mapper = n;
            mappers.put(type, n);
        }

        return mapper.map(id);
    }

    @NotNull
    private static Mapper createMapper(IdType type) {
        if (!type.persist) return new Mapper();

        Path path = Main.getMapFile(type.name());
        if (path.toFile().exists()) {
            try (var is = new DataInputStream(new FileInputStream(path.toFile()))) {
                return fromMaps(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("[WARN] Mapping for " + type + " not found! (this is normal for the very first run)");
        return new Mapper();
    }

    private static Mapper fromMaps(DataInputStream is) throws IOException {
        HashMap<Integer, Integer> maps = new HashMap<>();

        int length = is.readInt();

        for (int i = 0; i < length; i++) {
            maps.put(is.readInt(), i);
        }

        return new Mapper(maps);
    }


    private final HashMap<Integer, Integer> orgToMapped;

    private Mapper() {
        this(new HashMap<>());
    }

    private Mapper(HashMap<Integer, Integer> map) {
        this.orgToMapped = map;
    }

    private int map(int original) {
        if (orgToMapped.containsKey(original)) return orgToMapped.get(original);

        int value = orgToMapped.size();

        orgToMapped.put(original, value);
        return value;
    }

    private Mapped<Integer> tryMap(int original) {
        return Mapped.of(orgToMapped.get(original));
    }

    private void writeMaps(DataOutputStream os) throws IOException {
        int[] originals = new int[orgToMapped.size()];

        for (Map.Entry<Integer, Integer> entry : orgToMapped.entrySet()) {
            originals[entry.getValue()] = entry.getKey();
        }

        os.writeInt(originals.length);
        for (int original : originals) os.writeInt(original);
    }


}
