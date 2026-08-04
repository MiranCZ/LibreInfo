package me.miran.util;

import me.miran.Main;
import me.miran.parsing.PostMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

public class PostsSource {

    private static final String DEFAULT_URL = "http://gtfs-rt:8080/posts";
    private static final int ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    private PostsSource() {
    }

    /**
     * Fetches posts from gtfs-rt and caches the response in the state dir. Falls back
     * to the cached copy when the fetch fails or returns something unusable.
     *
     * @throws IOException when no usable posts can be obtained from either source
     */
    public static List<PostMapper.Post> resolve() throws IOException {
        Path cache = Main.getPostsFile().toPath();

        try {
            byte[] fetched = fetch();

            // parse before storing, so a well-formed response carrying the wrong
            // payload cannot replace a usable cache
            List<PostMapper.Post> posts = PostMapper.parse(fetched);
            store(fetched, cache);

            return posts;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            if (!Files.exists(cache)) {
                throw new IOException("Failed to obtain posts and no cached copy exists at " + cache, e);
            }

            System.out.println("WARN: could not use fetched posts (" + describe(e) + "), falling back to " + cache);
            return PostMapper.parse(Files.readAllBytes(cache));
        }
    }

    private static byte[] fetch() throws IOException, InterruptedException {
        URI uri = URI.create(System.getenv().getOrDefault("POSTS_URL", DEFAULT_URL));

        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        IOException lastFailure = null;

        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            try {
                HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());

                if (response.statusCode() != 200) {
                    throw new IOException(uri + " returned HTTP " + response.statusCode());
                }
                if (response.body().length == 0) {
                    throw new IOException(uri + " returned an empty body");
                }

                System.out.println("Fetched posts from " + uri + " (" + response.body().length + " bytes)");
                return response.body();
            } catch (IOException e) {
                lastFailure = e;
                System.out.println("Posts fetch attempt " + attempt + "/" + ATTEMPTS + " failed: " + describe(e));
            }

            // gtfs-parser is started directly by gtfs-updater, bypassing compose's depends_on,
            // so on a cold boot gtfs-rt may not be serving yet
            if (attempt < ATTEMPTS) {
                Thread.sleep(RETRY_DELAY.toMillis());
            }
        }

        throw lastFailure;
    }

    // connection failures out of HttpClient often carry a null message, leaving nothing to go on
    private static String describe(Exception e) {
        String message = e.getMessage();
        return message == null ? e.getClass().getSimpleName() : e.getClass().getSimpleName() + ": " + message;
    }

    private static void store(byte[] posts, Path cache) throws IOException {
        Path tmp = cache.resolveSibling(cache.getFileName() + ".tmp");

        Files.write(tmp, posts);
        Files.move(tmp, cache, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

}
