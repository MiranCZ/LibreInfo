package io.github.mirancz.gtfs.export.writer;

import io.github.mirancz.gtfs.ParsedGtfs;
import io.github.mirancz.gtfs.export.SingleWriter;
import io.github.mirancz.gtfs.remap.IdType;
import io.github.mirancz.gtfs.remap.Mapper;
import io.github.mirancz.request.ids.ApiPost;
import io.github.mirancz.util.CheckedOutputStream;

import java.io.IOException;
import java.util.*;

public class PostWriter extends SingleWriter {


    private final List<ApiPost> posts;

    public PostWriter(List<ApiPost> posts) {
        super("posts");
        this.posts = posts;
    }

    @Override
    protected void write(ParsedGtfs gtfs, CheckedOutputStream os) throws IOException {
        os.writeInt(posts.size());


        for (ApiPost e : posts) {
            // VERY careful here, we need to use the mapped stop id
            int stopId = Mapper.map(IdType.STOP, e.getStopId());

            os.writeShort(stopId);
        }

        for (ApiPost e : posts) {
            os.writeShort(e.getPostId());
        }

        for (ApiPost e : posts) {
            os.writeLocation(e.getLatitude(), e.getLongitude());
        }

        // post names are quite repetitive (can get down to <50%)
        // does not help with export since compression carries anyway, but worth if device storage becomes an issue
        for (ApiPost e : posts) {
            os.writeString(e.getName());
        }

    }
}
