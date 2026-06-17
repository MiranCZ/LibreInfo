package me.miran.libreinfo.parsing.storage;

import me.miran.libreinfo.R;
import me.miran.libreinfo.exception.AppException;
import me.miran.libreinfo.exception.ErrorType;
import me.miran.libreinfo.parsing.types.Location;
import me.miran.libreinfo.parsing.types.Post;
import me.miran.libreinfo.parsing.types.stop.Stop;
import me.miran.libreinfo.util.AppInputStream;
import me.miran.libreinfo.util.AppLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostStorage {

    public static PostStorage parse(AppInputStream array, StopStorage stopStorage) throws AppException {
        List<Post> posts;
        try(array) {
            posts = Post.parsePosts(array, stopStorage);
        } catch (IOException e) {
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
        }

        return new PostStorage(posts, stopStorage);
    }

    private final List<Post>[] postsForStop;
    private final List<Post> posts;
    private final StopStorage stopStorage;

    public PostStorage(List<Post> posts, StopStorage stopStorage) {
        posts = new ArrayList<>(posts);

        this.stopStorage = stopStorage;
        //noinspection unchecked
        this.postsForStop = new List[stopStorage.mapper.internalStopsLength()];
        for (int i = 0; i < postsForStop.length; i++) {
            postsForStop[i] = new ArrayList<>();
        }

        for (Iterator<Post> iterator = posts.iterator(); iterator.hasNext(); ) {
            Post post = iterator.next();
            Stop stop = post.stop();
            if (stop == Stop.NONE) {
                iterator.remove();
                continue;
            }

            postsForStop[stop.id.internal()].add(post);
        }

        this.posts = posts;
    }

    public Post getPost(int stopID, int postID) {
        for (Post post : getPosts(stopID)) {
             if (post.postID() == postID) return post;
        }

        Post dummyPost = new Post(Stop.NONE, postID, postID+". nastupiste", Location.NONE);
        AppLog.w("Unable to find post with args "+stopID + " ; "+postID + " ; "+getPosts(stopID));

        postsForStop[stopID].add(dummyPost);
        return dummyPost;
    }

    public List<Post> getPosts(Stop stop) {
        return getPosts(stop.id.internal());
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
