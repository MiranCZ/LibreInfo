package io.github.mirancz.libreinfo.codec.util;

import io.github.mirancz.libreinfo.model.types.Date;
import io.github.mirancz.libreinfo.model.types.Location;

import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AppInputStream extends DataInputStream {

    public AppInputStream(InputStream in) {
        super(in);
    }

    public Color readColor() throws IOException {
        int red = read();
        int green = read();
        int blue = read();

        return new Color(red, green, blue);
    }

    public Date readDate() throws IOException {
        byte day = readByte();
        byte month = readByte();
        short year = readShort();

        return new Date(day, month, year);
    }

    public Location readLocation() throws IOException {
        float lat = readInt();
        float lon = readInt();

        return new Location(lat / 1e6, lon / 1e6);
    }

    public String readString() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        int b;
        while ((b = read()) != 0) {
            if (b == -1) {
                throw new EOFException("Stream ended before NUL terminator");
            }
            bytes.write(b);
        }

        return bytes.toString(StandardCharsets.UTF_8);
    }

}
