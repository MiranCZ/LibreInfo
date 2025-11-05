package me.miran.mhdstuff.parsing.storage;

import me.miran.mhdstuff.parsing.types.Location;
import me.miran.mhdstuff.parsing.types.Post;
import me.miran.mhdstuff.parsing.types.Stop;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PostStorage {

    public static PostStorage parse(DataInputStream array, LineStorage lineStorage, StopStorage stopStorage) {
        List<Post> posts = null;
        try {
            posts = Post.parsePosts(array, lineStorage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new PostStorage(posts, stopStorage);
    }

    private final Map<Integer, List<Post>> postsForStop = new HashMap<>();
    private final List<Post> posts;
    private final StopStorage stopStorage;

    public PostStorage(List<Post> posts, StopStorage stopStorage) {
        posts = new ArrayList<>(posts);

        this.stopStorage = stopStorage;
        for (Iterator<Post> iterator = posts.iterator(); iterator.hasNext(); ) {
            Post post = iterator.next();
            Stop stop = stopStorage.getStop(post.stopID());
            if (stop == Stop.NONE) {
                iterator.remove();
                continue;
            }

            postsForStop.computeIfAbsent(stop.id, k -> new ArrayList<>()).add(post);
        }

        this.posts = posts;
    }

    public Post getPost(int stopID, int postID) {
        for (Post post : getPosts(stopID)) {
             if (post.postID() == postID) return post;
        }

        Post dummyPost = new Post(stopID, postID, postID+". nastupiste", Location.NONE, true, List.of());
        System.out.println("[WARN] Unable to find post with args "+stopID + " ; "+postID + " ; "+getPosts(stopID));

        postsForStop.computeIfAbsent(stopID, k -> new ArrayList<>()).add(dummyPost);
        return dummyPost;
    }

    public List<Post> getPosts(Stop stop) {
        return getPosts(stop.id);
    }

    public List<Post> getPosts(int stopId) {
        if (!postsForStop.containsKey(stopId)) {
            System.out.println("[WARN] Tried to get posts for unregistered stop! " + stopId);
            return List.of();
        }

        return postsForStop.getOrDefault(stopId, List.of());
    }


    public List<Post> getAllPosts() {
        return posts;
    }
}
