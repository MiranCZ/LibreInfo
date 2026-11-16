package io.github.mirancz.update;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import io.github.mirancz.gtfs.Parser;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

import static io.github.mirancz.Main.*;

public class Updater {

    record DataResult(boolean updated, String hash) {
    }

    private static final String GTFS_URL = "https://kordis-jmk.cz/gtfs/gtfs.zip";
    private static final String OSM_URL = "https://download.geofabrik.de/europe/czech-republic/jihomoravsky-latest.osm.pbf";

    private static final Path ETAG_FILE = STATE_DIR.resolve("gtfs_etag");

    private static final Path OSM_PATH = OSM_DIR.resolve("region.osm.pbf");
    private static final Path OSM_HASH = OSM_DIR.resolve("gtfs_hash");

    private static final Path MOTIS_IMPORT_FILE = STATE_DIR.resolve("motis_imported");

    private static final HttpClient HTTP;

    static {
        HTTP = HttpClient
                .newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public static void update() throws Exception {
        try (var client = DockerClientBuilder.getInstance().build()) {
            Compose compose = new Compose(client);

            DataResult gtfs = checkAndDownloadGtfs();
            DataResult osm = checkAndDownloadOsm();

            System.out.println("Gtfs state: "+gtfs);
            System.out.println("Osm state: "+osm);

            String motisKey = gtfs.hash + ";" + osm.hash;

            if (!gtfs.updated && !osm.updated) {
                System.out.println("Nothing updated");

                if (!Files.exists(MOTIS_IMPORT_FILE) || !Files.readString(MOTIS_IMPORT_FILE).equals(motisKey)) {
                    System.out.println("Motis import not run yet.., running now");

                    runMotisImport(compose, motisKey);

                    restartMotis(compose);
                } else {
                    ensureMotisRunning(compose);
                }

                return;
            }

            if (gtfs.updated) {
                System.out.println("Gtfs updated, parsing");

                Parser.parseGtfs();
            }

            runMotisImport(compose, motisKey);

            restartMotis(compose);
        } finally {
            System.out.println("Update done");
        }
    }


    private static void runMotisImport(Compose compose, String key) throws IOException {
        System.out.println("RUNNING IMPORT yay");

        var container = compose.get("motis-import");

        compose.client().startContainerCmd(container.getId()).exec();

        // TODO
        compose.client().logContainerCmd(container.getId()).withStdOut(true).withStdErr(true).withFollowStream(true).exec(new ResultCallback<Frame>() {
            @Override
            public void onStart(Closeable closeable) {
            }

            @Override
            public void onNext(Frame object) {
                System.out.println("[Motis-import] " + new String(object.getPayload()));
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void close() throws IOException {
            }
        });


        int statusCode = compose.client().waitContainerCmd(container.getId()).start().awaitStatusCode();

        System.out.println("I guess ran import");

        if (statusCode != 0) {
            throw new RuntimeException("Non-zero status code" + statusCode);
        }

        Files.writeString(MOTIS_IMPORT_FILE, key);
    }

    private static void restartMotis(Compose compose) {
        System.out.println("Restarting motis");

        var container = compose.get("motis");

        if (container.getState().equals("running")) {
            compose.client().stopContainerCmd(container.getId()).exec();
            ensureMotisRunning(compose);
        } else {
            compose.client().restartContainerCmd(container.getId()).exec();
        }
    }

    private static void ensureMotisRunning(Compose compose) {
        var container = compose.get("motis");

        if (!container.getState().equals("running")) {
            System.out.println("Starting motis");

            compose.client().startContainerCmd(container.getId()).exec();
        }
    }

    private static DataResult checkAndDownloadGtfs() throws IOException {
        URI uri = URI.create(GTFS_URL);

        var builder = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(120));

        if (Files.exists(ETAG_FILE) && Files.exists(GTFS_ZIP_PATH)) {
            builder = builder.header("If-None-Match", Files.readString(ETAG_FILE).strip());
        }

        HttpRequest request = builder.GET().build();
        try {
            HttpResponse<InputStream> response = HTTP.send(request, HttpResponse.BodyHandlers.ofInputStream());

            String originalHash;

            if (Files.exists(GTFS_ZIP_PATH)) {
                originalHash = md5Hash(Files.newInputStream(GTFS_ZIP_PATH));
            } else {
                originalHash = "-";
            }
            if (response.statusCode() == 304) {
                // not modified
                return new DataResult(false, originalHash);
            }

            if (response.statusCode() != 200) {
                throw new IllegalStateException("API returned HTTP " + response.statusCode() + " for GTFS url");
            }

            var etag = response.headers().firstValue("etag");
            if (etag.isPresent()) {
                Files.writeString(ETAG_FILE, etag.get().strip());
            } else {
                Files.deleteIfExists(ETAG_FILE);
            }

            var tmp = GTFS_ZIP_PATH.resolveSibling(GTFS_ZIP_PATH.getFileName() + ".tmp");

            try (var is = response.body()) {
                Files.write(tmp, is.readAllBytes());
            }

            String newHash = md5Hash(Files.newInputStream(tmp));

            if (originalHash.equals(newHash)) {
                return new DataResult(false, originalHash);
            }

            Files.move(tmp, GTFS_ZIP_PATH, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            return new DataResult(true, newHash);
        } catch (IOException e) {
            throw new UncheckedIOException("Request to " + uri + " failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Request to " + uri + " was interrupted", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static DataResult checkAndDownloadOsm() {
        var hashUrl = OSM_URL + ".md5";

        try {
            String newHash = HTTP.send(
                    HttpRequest
                            .newBuilder(URI.create(hashUrl))
                            .timeout(Duration.ofSeconds(120))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            ).body();

            String oldHash;
            if (Files.exists(OSM_HASH)) {
                oldHash = Files.readString(OSM_HASH);
            } else {
                oldHash = "-";
            }

            if (newHash.equals(oldHash)) {
                return new DataResult(false, oldHash);
            }


            HttpResponse<InputStream> response = HTTP.send(HttpRequest.newBuilder(URI.create(OSM_URL)).build(), HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new IllegalStateException("API returned HTTP " + response.statusCode() + " for GTFS url");
            }

            var tmp = OSM_PATH.resolveSibling(OSM_PATH.getFileName() + ".tmp");

            try (var is = response.body()) {
                Files.write(tmp, is.readAllBytes());
            }

            Files.writeString(OSM_HASH, newHash);

            Files.move(tmp, OSM_PATH, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            return new DataResult(true, newHash);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String md5Hash(InputStream is) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (DigestInputStream dis = new DigestInputStream(is, md)) {
            dis.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        is.close();

        byte[] bytes = md.digest();

        return HexFormat.of().formatHex(bytes);
    }

}
