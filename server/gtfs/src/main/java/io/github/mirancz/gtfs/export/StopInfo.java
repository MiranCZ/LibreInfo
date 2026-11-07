package io.github.mirancz.gtfs.export;

import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream;
import io.github.mirancz.libreinfo.model.types.PostType;

import java.io.IOException;

// TODO save N vs Z
public record StopInfo(short stopId, short postId, PostType type) {

    public void write(CheckedOutputStream os) throws IOException {
        os.writeShort(stopId);
        os.writeShort(postId);
    }

}

