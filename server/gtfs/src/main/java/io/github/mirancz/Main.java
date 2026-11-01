package io.github.mirancz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.mirancz.debug.CompressionStats;
import io.github.mirancz.gtfs.ApiParser;
import io.github.mirancz.gtfs.ParsedGtfs;
import io.github.mirancz.gtfs.export.writer.*;
import io.github.mirancz.gtfs.export.Writer;
import io.github.mirancz.gtfs.ids.*;
import io.github.mirancz.gtfs.remap.Mapper;
import io.github.mirancz.request.*;
import io.github.mirancz.request.ids.ApiPost;
import io.github.mirancz.request.ids.PostsResponse;
import io.github.mirancz.request.transform.DiversionMapper;
import io.github.mirancz.request.transform.EventMapper;
import io.github.mirancz.util.CheckedOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;
import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


import static spark.Spark.*;

public class Main {
    private static final Path GTFS_ZIP_PATH = Path.of("/data/raw/gtfs.zip");


    private static final Path OUTPUT_DIR = Path.of("/public");
    
    private static final Path STATE_DIR = Path.of("/state");
    private static final Path MAP_DIR = STATE_DIR.resolve("maps");

    private static final long MAGIC = 0xF5444A4D0D0A1A00L;
    private static final int DATA_VERSION = 1;

    public static Poller<String> diversionPoller = new Poller<>(
            "diversions",
            () -> "{\"diversions\":" + new Gson().toJson(DiversionMapper.mapDiversions(RequestHelper.getDiversions())) + "}",
            Duration.ofMinutes(5)
    );

    public static Poller<String> eventPoller = new Poller<>(
            "events",
            () -> "{\"events\":" + new Gson().toJson(EventMapper.mapEvents(RequestHelper.getEvents())) + "}",
            Duration.ofSeconds(45)
    );


    static void main() throws Exception {
        parseGtfs();

        setupSpark();
    }

    private static void setupSpark() {
        ipAddress("0.0.0.0");
        port(8080);

        Gson gson = new GsonBuilder()
                .create();


        diversionPoller.start();
        eventPoller.start();

        VehicleSocket socket = new VehicleSocket(gson);
        webSocket("/stream", socket);

        var stream = new VehicleStore(gson, socket);

        var vehiclesPoller = new Poller<>("vehicles", RequestHelper::getVehicles, stream::onData, Duration.ofSeconds(5));
        vehiclesPoller.start();

        Endpoints endpoints = new Endpoints(stream, gson);

        get("/vehicles", endpoints::handleVehicles);
        get("/vehicleInfo", endpoints::handleVehicleInfo);
        get("/stopDelays", endpoints::handleStopDelays);
        get("/routeDelays", endpoints::handleRouteDelays);
        get("/departures", endpoints::handleDepartures);

        get("/diversions", endpoints::handleDiversions);
        get("/events", endpoints::handleEvents);
        get("/posts", endpoints::handlePosts);

        get("/routePath", endpoints::routePath);
    }

    private static void parseGtfs() throws IOException {
        if (!Files.exists(GTFS_ZIP_PATH)) {
            throw new FileNotFoundException("GTFS zip not found at " + GTFS_ZIP_PATH);
        }


        List<ApiPost> posts = RequestHelper.getPosts().posts();
//        List<ApiPost> posts = new Gson().fromJson(Files.readString(STATE_DIR.resolve("posts.json")), PostsResponse.class).posts();

        Path mapped = getMappedGtfsFile();
        Path tmp = mapped.resolveSibling(mapped.getFileName() + ".tmp");

        Files.createDirectories(mapped.getParent());

        ParsedGtfs data = handleGtfs(tmp);

        List<Writer> writers = List.of(
                new ApiWriter(),
                new CalendarWriter(),
                new LineInfoWriter(),
                new StopWriter(),
                new TripWriter(),

                // not from GTFS, but from proprietary API buuut whatever
                new PostWriter(posts)
        );

        // XZOutputStream compressedOutput = new XZOutputStream(new FileOutputStream(getDataFile()), new LZMA2Options());
        CompressionStats stats = new CompressionStats();

        var options = new LZMA2Options();
        options.setNiceLen(273);
        XZOutputStream compressedOutput = new XZOutputStream(stats.track(new FileOutputStream(new File("test.txt"))), options);
        try(CheckedOutputStream output = new CheckedOutputStream(compressedOutput)) {
            Function<String, CheckedOutputStream> outputProvider = s -> getWrappedDataOutputStream(s, output, stats, compressedOutput);

            writeHeader(output);

            for (Writer writer : writers) {
                writer.write(data, outputProvider);
            }
        }
        stats.printTotal();

        Files.move(tmp, mapped, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        Mapper.savePersistentMaps();
    }

    private static ParsedGtfs handleGtfs(Path mappedPath) throws IOException {
        record Entry<T>(Class<T> clazz, String name, List<T> values) {

            public void addAll(List<?> x) {
                values.addAll((Collection<? extends T>) x);
            }

        }

        List<ApiEntry> api = new ArrayList<>();
        List<StopEntry> stops = new ArrayList<>();
        List<CalendarEntry> calendar = new ArrayList<>();
        List<CalendarDateEntry> calendarDates = new ArrayList<>();
        List<RouteEntry> routes = new ArrayList<>();
        List<StopTimeEntry> stopTimes = new ArrayList<>();
        List<TransferEntry> transfers = new ArrayList<>();
        List<TripEntry> trips = new ArrayList<>();

        List<Entry<?>> entries = new ArrayList<>() {{
            // we do not need this; only for the rewritten gtfs
            add(new Entry<>(AgencyEntry.class, "agency.txt", new ArrayList<>()));

            add(new Entry<>(StopEntry.class, "stops.txt", stops));
            add(new Entry<>(CalendarEntry.class, "calendar.txt", calendar));
            add(new Entry<>(CalendarDateEntry.class, "calendar_dates.txt", calendarDates));
            add(new Entry<>(RouteEntry.class, "routes.txt", routes));
            add(new Entry<>(StopTimeEntry.class, "stop_times.txt", stopTimes));
            add(new Entry<>(TransferEntry.class, "transfers.txt", transfers));
            add(new Entry<>(TripEntry.class, "trips.txt", trips));
        }};

        Map<String, Entry<?>> extractors = new HashMap<>();
        for (Entry<?> entry : entries) {
            extractors.put(entry.name, entry);
        }

        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(mappedPath)))) {
            try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(GTFS_ZIP_PATH))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    String name = entry.getName();
                    InputStream is = new ByteArrayInputStream(zip.readAllBytes());

                    // their own file not compliant with GTFS and **not** a csv (obviously)
                    if (name.equals("api.txt")) {
                        api = ApiParser.parse(is);
                    } else if (extractors.containsKey(name)) {
                        var e = extractors.get(name);

                        ByteArrayOutputStream remapOs = new ByteArrayOutputStream();
                        List<?> result = remapAndGet(e.clazz, is, remapOs);

                        e.addAll(result);
                        out.putNextEntry(new ZipEntry(name));
                        remapOs.writeTo(out);
                        out.closeEntry();
                    }
                }
            }
        }

        return new ParsedGtfs(
                api,
                stops,
                calendar,
                calendarDates,
                routes,
                stopTimes,
                transfers,
                trips
        );
    }

    private static <T> List<T> remapAndGet(Class<T> clazz, InputStream input, OutputStream os) {
        CsvMapper mapper = CsvMapper.builder().build();
        CsvSchema schema = mapper.schemaFor(clazz).withHeader();

        List<T> res;
        try (MappingIterator<T> it = mapper
                .readerFor(clazz)
                .with(schema)
                .readValues(input)) {

            res = it.readAll();
        }

        try (var writer = mapper.writerFor(clazz).with(schema).writeValues(os)) {
            writer.writeAll(res);
        }

        return res;
    }

    private static CheckedOutputStream getWrappedDataOutputStream(String s, CheckedOutputStream output, CompressionStats stats, XZOutputStream compressed) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        return new CheckedOutputStream(os) {
            @Override
            public void close() throws IOException {
                byte[] data = os.toByteArray();

                stats.beginSection(compressed);

                output.writeBoolean(true);

                output.writeString(s);

                output.writeInt(data.length);
                output.write(data);

                stats.endSection(s, data, compressed);

                super.close();
            }
        };
    }

    private static void writeHeader(CheckedOutputStream output) throws IOException {
        output.writeLong(MAGIC);

        output.writeInt(DATA_VERSION);

        int date = Integer.parseInt(new SimpleDateFormat("HHddMMyy").format(new Date()));

        output.writeInt(date);
    }

    public static Path getMapFile(String name) {
        return MAP_DIR.resolve(name);
    }

    public static Path getMappedGtfsFile() {
        return OUTPUT_DIR.resolve("gtfs.zip");
    }

}
