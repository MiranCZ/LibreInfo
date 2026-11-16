package io.github.mirancz.libreinfo.parsing.storage;

import io.github.mirancz.libreinfo.codec.PostsCodec;
import io.github.mirancz.libreinfo.codec.util.AppInputStream;
import io.github.mirancz.libreinfo.exception.AppException;
import io.github.mirancz.libreinfo.model.Post;
import io.github.mirancz.libreinfo.model.types.Location;
import io.github.mirancz.libreinfo.model.Stop;
import io.github.mirancz.libreinfo.util.AppLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostStorage implements AppStorage {

    public static PostStorage parse(AppInputStream array, StopStorage stopStorage) throws AppException {
        List<Post> posts;
        try {
            posts = PostsCodec.read(array);
        } catch (IOException e) {
            throw AppException.dataLoad(e);
        }

        return new PostStorage(posts, stopStorage);
    }

    private final List<Post>[] postsForStop;
    private final List<Post> posts;

    public PostStorage(List<Post> posts, StopStorage stopStorage) {
        posts = new ArrayList<>(posts);

        //noinspection unchecked
        this.postsForStop = new List[stopStorage.getStopsLengths()];
        for (int i = 0; i < postsForStop.length; i++) {
            postsForStop[i] = new ArrayList<>();
        }

        for (Iterator<Post> iterator = posts.iterator(); iterator.hasNext(); ) {
            Post post = iterator.next();
            Stop stop = stopStorage.getStop(post.getStopId());
            if (stop == Stop.Companion.getNONE()) {
                iterator.remove();
                continue;
            }

            postsForStop[stop.getId()].add(post);
        }

        this.posts = posts;
    }

    public Post getPost(short stopId, short postId) {
        for (Post post : getPosts(stopId)) {
             if (post.getPostId() == postId) return post;
        }

        Post dummyPost = new Post(-1, postId, postId+". nastupiste", Location.getNONE());
        AppLog.w("Unable to find post with args "+stopId + " ; "+postId + " ; "+getPosts(stopId));

        postsForStop[stopId].add(dummyPost);
        return dummyPost;
    }

    public List<Post> getPosts(Stop stop) {
        return getPosts(stop.getId());
    }

    public List<Post> getPosts(int stopId) {
        if (stopId < 0 || stopId >= postsForStop.length) {
            AppLog.w("Tried to get posts for unregistered stop! " + stopId);
            return List.of();
        }

        return postsForStop[stopId];
    }


    public List<Post> getAllPosts() {
        return posts;
    }
}
