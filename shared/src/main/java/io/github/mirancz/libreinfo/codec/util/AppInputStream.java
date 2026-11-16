package io.github.mirancz.libreinfo.codec.util;

import io.github.mirancz.libreinfo.model.types.Color;
import io.github.mirancz.libreinfo.model.types.Date;
import io.github.mirancz.libreinfo.model.types.Location;

import java.io.*;
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
        double lat = readInt();
        double lon = readInt();

        return new Location(lat / 1e6, lon / 1e6);
    }

    public int readVarInt() throws IOException {
        int i = 0;
        int j = 0;
        byte b0;

        do {
            b0 = readByte();
            i |= (b0 & 127) << j++ * 7;
        }
        while ((b0 & 128) == 128);

        return i;
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

        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

}
