package me.miran.util;

import java.io.IOException;

public record StopInfo(short stopId, short postId) {

    public void write(CheckedOutputStream os) throws IOException {
        os.writeShort(stopId);
        os.writeShort(postId);
    }

}
