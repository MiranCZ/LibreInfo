package com.example.mhdstuff.parsing.storage;

import com.example.mhdstuff.parsing.types.Post;
import com.example.mhdstuff.parsing.types.Stop;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PostStorage {

    public static PostStorage parse(JsonArray array, LineStorage lineStorage, StopStorage stopStorage) {
        List<Post> posts = Post.parsePosts(array, lineStorage);

        return new PostStorage(posts, stopStorage);
    }

    private final Map<Stop, List<Post>> postsForStop = new HashMap<>();
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

            postsForStop.computeIfAbsent(stop, k -> new ArrayList<>()).add(post);
        }

        this.posts = posts;
    }

    public Post getPost(int stopID, int postID) {
        for (Post post : getPosts(stopID)) {
             if (post.postID() == postID) return post;
        }

        System.out.println("[WARN] Unable to find post with args "+stopID + " ; "+postID + " ; "+getPosts(stopID));
        return null;
    }

    public List<Post> getPosts(int id) {
        return getPosts(stopStorage.getStop(id));
    }

    public List<Post> getPosts(Stop stop) {
        if (!postsForStop.containsKey(stop)) {
            System.out.println("[WARN] Tried to get posts for unregistered stop! " + stop);
            return List.of();
        }

        return postsForStop.getOrDefault(stop, List.of());
    }


    public List<Post> getAllPosts() {
        return posts;
    }
}
