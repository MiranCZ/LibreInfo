package me.miran.parsing;

import me.miran.util.IdStorage;
import me.miran.util.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Rewrites a GTFS zip so that its ids are small sequential ints, which is what we feed to MOTIS
 * and what the parsers read.
 * <p>
 * stops keep the {@code U<stop>[ZN]<post>} form, with the stop number mapped through
 * {@link IdStorage#STOP} (so it is stable across feed updates). Posts are left as they are.</li>
 *
 */
public class GtfsRewriter {

    private enum Kind {SERVICE, TRIP, BLOCK, STOP}

    private static final Map<String, Kind> COLUMNS = Map.of(
            "service_id", Kind.SERVICE,
            "trip_id", Kind.TRIP,
            "from_trip_id", Kind.TRIP,
            "to_trip_id", Kind.TRIP,
            "block_id", Kind.BLOCK,
            "stop_id", Kind.STOP,
            "parent_station", Kind.STOP,
            "from_stop_id", Kind.STOP,
            "to_stop_id", Kind.STOP
    );

    /**
     * Files are processed in this order so that ids are numbered in the order of the
     * file that defines them. Other .txt files are rejected (see {@link #run}).
     */
    private static final List<String> FILES = List.of(
            "agency.txt", "routes.txt", "calendar.txt", "calendar_dates.txt",
            "stops.txt", "trips.txt", "stop_times.txt", "transfers.txt",
            "shapes.txt", "frequencies.txt", "feed_info.txt");

    private static final String API_FILE = "api.txt";

    private final Map<String, Integer> services = new HashMap<>();
    private final Map<String, Integer> trips = new HashMap<>();
    private final Map<String, Integer> blocks = new HashMap<>();

    private GtfsRewriter() {
    }

    /**
     * @param serviceMapsFile the service id table, for gtfs-rt (it has to translate the service ids of the upstream API)
     */
    public static void rewrite(Path input, Path output, Path serviceMapsFile) throws IOException {
        Path tmp = output.resolveSibling(output.getFileName() + ".tmp");
        GtfsRewriter rewriter = new GtfsRewriter();

        try (ZipFile zip = new ZipFile(input.toFile());
             ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            rewriter.run(zip, out);
        }

        Files.move(tmp, output, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        rewriter.writeServiceMaps(serviceMapsFile);
    }

    /** Same layout as the stop table: the count, then the original service_id of every new id, in order. */
    private void writeServiceMaps(Path file) throws IOException {
        int[] originals = new int[services.size()];
        services.forEach((original, id) -> originals[id] = Integer.parseInt(original.strip()));

        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            os.writeInt(originals.length);
            for (int original : originals) os.writeInt(original);
        }

        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private void run(ZipFile zip, ZipOutputStream out) throws IOException {
        for (String name : FILES) {
            ZipEntry entry = zip.getEntry(name);
            if (entry != null) rewriteFile(zip, entry, out);
        }

        // after the csv files, so every trip id is known
        ZipEntry api = zip.getEntry(API_FILE);
        if (api != null) rewriteApi(zip, api, out);

        // any other standard gtfs file we don't know the ids of must not be silently dropped
        for (ZipEntry entry : Collections.list(zip.entries())) {
            String name = entry.getName();
            if (FILES.contains(name) || !name.endsWith(".txt") || name.equals(API_FILE)) continue;

            System.out.println("[WARN] Unknown GTFS file, cannot remap its ids:" + name);
        }
    }

    private String map(Kind kind, String value) {
        return switch (kind) {
            case SERVICE -> Integer.toString(services.computeIfAbsent(value, k -> services.size()));
            case TRIP -> Integer.toString(trips.computeIfAbsent(value, k -> trips.size()));
            case BLOCK -> Integer.toString(blocks.computeIfAbsent(value, k -> blocks.size()));
            case STOP -> mapStop(value);
        };
    }

    /**
     * {@code U<stop>Z<post>} (or {@code N}) -> same form, with only the stop number mapped.
     */
    private static String mapStop(String stopUID) {
        if (!stopUID.startsWith("U")) {
            throw new IllegalStateException("Invalid stop UID: " + stopUID);
        }

        int ind = Math.max(stopUID.indexOf('Z'), stopUID.indexOf('N'));
        if (ind == -1) throw new IllegalArgumentException("Invalid stop UID: " + stopUID);

        int stopId = Integer.parseInt(stopUID.substring(1, ind));

        return "U" + IdStorage.STOP.getId(stopId) + stopUID.substring(ind);
    }

    private void rewriteFile(ZipFile zip, ZipEntry entry, ZipOutputStream out) throws IOException {
        out.putNextEntry(new ZipEntry(entry.getName()));

        BufferedReader in = new BufferedReader(new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8));
        Writer w = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

        List<String> header = readRecord(in);
        if (header == null) throw new IllegalStateException("Empty file: " + entry.getName());
        if (!header.isEmpty() && header.getFirst().startsWith("﻿")) {
            header.set(0, header.getFirst().substring(1));
        }
        writeRecord(w, header);

        Kind[] kinds = new Kind[header.size()];
        for (int i = 0; i < kinds.length; i++) kinds[i] = COLUMNS.get(header.get(i));

        List<String> record;
        while ((record = readRecord(in)) != null) {
            for (int i = 0; i < record.size() && i < kinds.length; i++) {
                String value = record.get(i);
                if (kinds[i] != null && !value.isEmpty()) {
                    record.set(i, map(kinds[i], value));
                }
            }
            writeRecord(w, record);
        }

        w.flush(); // not closed: that would close the zip stream
        out.closeEntry();
    }

    /**
     * api.txt lines look like {@code Linka/CVlaku = trip_id: 104/4 = 1}, the last number being the trip id.
     */
    private void rewriteApi(ZipFile zip, ZipEntry entry, ZipOutputStream out) throws IOException {
        out.putNextEntry(new ZipEntry(entry.getName()));

        BufferedReader in = new BufferedReader(Utils.detectCharset(zip.getInputStream(entry)));
        Writer w = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

        String line;
        while ((line = in.readLine()) != null) {
            if (line.startsWith(ApiParser.START_TEXT)) {
                int eq = line.lastIndexOf('=');
                String trip = line.substring(eq + 1).strip();

                Integer mapped = trips.get(trip);
                if (mapped == null) throw new IllegalStateException("api.txt has unknown trip id: " + trip);

                line = line.substring(0, eq + 1) + " " + mapped;
            }
            w.write(line);
            w.write('\n');
        }

        w.flush();
        out.closeEntry();
    }

    /**
     * Reads one RFC 4180 record (quoted fields may contain commas, quotes and newlines). Null at EOF.
     */
    private static List<String> readRecord(BufferedReader in) throws IOException {
        int c = in.read();
        while (c == '\r' || c == '\n') c = in.read(); // blank lines
        if (c == -1) return null;

        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean quoted = false;

        for (; c != -1; c = in.read()) {
            if (quoted) {
                if (c != '"') {
                    sb.append((char) c);
                    continue;
                }
                in.mark(1);
                if (in.read() == '"') {
                    sb.append('"');
                } else {
                    in.reset();
                    quoted = false;
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                fields.add(sb.toString());
                sb.setLength(0);
            } else if (c == '\n') {
                break;
            } else if (c != '\r') {
                sb.append((char) c);
            }
        }

        fields.add(sb.toString());
        return fields;
    }

    private static void writeRecord(Writer w, List<String> fields) throws IOException {
        for (int i = 0; i < fields.size(); i++) {
            if (i != 0) w.write(',');

            String f = fields.get(i);
            if (f.indexOf(',') >= 0 || f.indexOf('"') >= 0 || f.indexOf('\n') >= 0 || f.indexOf('\r') >= 0) {
                w.write('"');
                w.write(f.replace("\"", "\"\""));
                w.write('"');
            } else {
                w.write(f);
            }
        }
        w.write('\n');
    }

}
