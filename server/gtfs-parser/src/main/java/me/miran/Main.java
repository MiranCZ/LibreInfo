package me.miran;


import me.miran.parsing.*;
import me.miran.util.CheckedOutputStream;
import me.miran.util.IdStorage;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {

    private static final Path GTFS_ZIP_PATH = Path.of( "/data/raw/gtfs.zip");
    private static final Path OUTPUT_DIR = Path.of("/public");
    private static final Path STATE_DIR = Path.of("/state");

    private static final List<Parser> parsers = List.of(
            new StopParser(), new LineInfoParser(), new TripParser(),
            new ApiParser(), new CalendarParser(), new TransfersParser());

    public static void main(String[] args) throws Exception {
        parse();
    }

    public static synchronized void parse() throws Exception {
        if (!Files.exists(GTFS_ZIP_PATH)) {
            throw new FileNotFoundException("GTFS zip not found at " + GTFS_ZIP_PATH);
        }

        createOutputDir();

        XZOutputStream compressedOutput = new XZOutputStream(new FileOutputStream(getDataFile()), new LZMA2Options());
        DataOutputStream output = new DataOutputStream(compressedOutput);

        Function<String, CheckedOutputStream> outputProvider = s -> getWrappedDataOutputStream(s, output);

        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(GTFS_ZIP_PATH.toFile()))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                BufferedInputStream bif = new BufferedInputStream(zip);
                bif.mark(Integer.MAX_VALUE);

                for (Parser parser : parsers) {
                    parser.onFile(name, bif, outputProvider);
                    bif.reset();
                }
            }

            for (Parser parser : parsers) {
                parser.onFinish(outputProvider);
            }
        }

        writeStopIdMaps(outputProvider);
        writePosts(outputProvider);

        output.writeBoolean(false);
        output.close();

        long byteSize = Files.size(getDataFile().toPath());
        Files.writeString(getInfoFile().toPath(), generateInfoString(byteSize));
    }

    private static void createOutputDir() throws IOException {
        Files.createDirectories(OUTPUT_DIR);
        Files.createDirectories(STATE_DIR);
    }

    private static void writePosts(Function<String, CheckedOutputStream> outputProvider) throws IOException {
        CheckedOutputStream os = outputProvider.apply("posts");

        os.write(Files.readAllBytes(getPostsFile().toPath()));

        os.close();
    }

    private static CheckedOutputStream getWrappedDataOutputStream(String s, DataOutputStream output) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        return new CheckedOutputStream(os) {
            @Override
            public void close() throws IOException {
                byte[] data = os.toByteArray();

                output.writeBoolean(true);

                byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
                output.writeInt(bytes.length);
                output.write(bytes);

                output.writeInt(data.length);
                output.write(data);
                super.close();
            }
        };
    }

    private static DataOutputStream getDataOutStream(File file) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        BufferedOutputStream outputStream = new BufferedOutputStream(fos);

        return new DataOutputStream(outputStream);
    }

    private static void writeStopIdMaps(Function<String, CheckedOutputStream> outputProvider) throws IOException {
        CheckedOutputStream os = outputProvider.apply("stop_mapping");
        IdStorage.STOP.write(os);
        os.close();

        DataOutputStream s = getDataOutStream(getStopMapsFile());
        IdStorage.STOP.write(new CheckedOutputStream(s));
        s.close();
    }

    private static String generateInfoString(long byteSize) {
        return "{" +
                "\"lastUpdated\":"+System.currentTimeMillis()+"," +
                "\"byteSize\":"+byteSize+
                "}";
    }

    public static File getDataFile() {
        return OUTPUT_DIR.resolve("data").toFile();
    }

    public static File getInfoFile() {
        return OUTPUT_DIR.resolve("info").toFile();
    }

    public static File getPostsFile() {
        return STATE_DIR.resolve("posts").toFile();
    }

    public static File getStopMapsFile() {
        return STATE_DIR.resolve("stop_maps").toFile();
    }

}
