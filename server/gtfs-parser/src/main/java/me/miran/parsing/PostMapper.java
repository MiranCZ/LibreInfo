package me.miran.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.miran.util.CheckedOutputStream;
import me.miran.util.IdStorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns the KORDIS posts response into the "posts" section: an int count followed by
 * {stopId short, postId short, name string, latitude double, longitude double} per post.
 */
public class PostMapper {

    private static final Gson GSON = new Gson();

    private PostMapper() {
    }

    public record Post(int stopId, int postId, String name, double latitude, double longitude) {
    }

    /**
     * Decodes the response and drops posts not serving the public. Kept separate from
     * {@link #write} so malformed input fails before the output stream is opened.
     */
    public static List<Post> parse(byte[] json) throws IOException {
        PostsResponse response;
        try {
            response = GSON.fromJson(new String(json, StandardCharsets.UTF_8), PostsResponse.class);
        } catch (JsonSyntaxException e) {
            throw new IOException("Posts response could not be read as JSON posts", e);
        }

        if (response == null || response.posts == null) {
            throw new IOException("Posts response has no 'posts' array");
        }

        List<Post> result = new ArrayList<>();

        for (ApiPost post : response.posts) {
            // non-public posts are operational (depot moves, detour terminals) and are
            // served by no line, so they are not passenger-facing
            if (!post.isPublic) continue;

            result.add(new Post(post.stopId, post.postId, post.name, post.latitude, post.longitude));
        }

        return result;
    }

    public static void write(List<Post> posts, CheckedOutputStream output) throws IOException {
        List<Post> mapped = new ArrayList<>(posts.size());
        int unknownStops = 0;

        for (Post post : posts) {
            // getExistingId, not getId: a miss would assign a fresh internal id, and the
            // stop mapping the client resolves against has already been written
            int stopId = IdStorage.STOP.getExistingId(post.stopId());

            if (stopId == -1) {
                unknownStops++;
                continue;
            }
            if (post.postId() > Short.MAX_VALUE) {
                throw new IOException("Post id " + post.postId() + " of stop " + post.stopId() + " exceeds a short");
            }

            mapped.add(new Post(stopId, post.postId(), post.name(), post.latitude(), post.longitude()));
        }

        output.writeInt(mapped.size());

        for (Post post : mapped) {
            output.writeShort(post.stopId());
            output.writeShort(post.postId());
            output.writeString(post.name());
            output.writeDouble(post.latitude());
            output.writeDouble(post.longitude());
        }

        System.out.println("Posts: wrote " + mapped.size() + ", skipped " + unknownStops + " with a stop missing from the feed");
    }

    private static class PostsResponse {
        List<ApiPost> posts;
    }

    private static class ApiPost {
        int stopId;
        int postId;
        String name;
        double latitude;
        double longitude;
        boolean isPublic;
    }

}
