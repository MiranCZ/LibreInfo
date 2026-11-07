package io.github.mirancz.libreinfo.codec.util;

import io.github.mirancz.libreinfo.model.types.Date;
import io.github.mirancz.libreinfo.model.types.Location;

import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Same as {@link DataOutputStream } but for smaller datatypes like {@code short} it checks the value fits its bounds
 */
public class CheckedOutputStream implements AutoCloseable {

    private final DataOutputStream os;

    public CheckedOutputStream(OutputStream os) {
        this(new DataOutputStream(os));
    }

    public CheckedOutputStream(DataOutputStream os) {
        this.os = os;
    }

    public void writeLong(long v) throws IOException {
        os.writeLong(v);
    }

    public void writeInt(int v) throws IOException {
        os.writeInt(v);
    }


    public void writeVarInt(int v) throws IOException {
        writeVarInt((long) v);
    }

    public void writeVarInt(long v) throws IOException {
        while ((v & ~0x7f) != 0) {
            write((int) ((v & 0x7f) | 0x80));
            v >>>= 7;
        }
        write((int) v);
    }



    public void writeShort(short v) throws IOException {
        writeShort((int) v);
    }

    public void writeShort(int v) throws IOException {
        if (v > Short.MAX_VALUE || v < Short.MIN_VALUE)
            throw new IllegalStateException("Attempted to write a short outside of range! ("+v+")");

        os.writeShort(v);
    }


    public void writeByte(byte v) throws IOException {
        writeByte((int) v);
    }

    public void writeByte(int v) throws IOException {
        if (v > Byte.MAX_VALUE || v < Byte.MIN_VALUE)
            throw new IllegalStateException("Attempted to write a byte outside of range! ("+v+")");

        os.writeByte(v);
    }

    public void write(int v) throws IOException {
        if (v < 0 || v > 0xFF)
            throw new IllegalStateException("Attempted to write an unsigned byte outside of range! ("+v+")");

        os.write(v);
    }

    public void writeDouble(double v) throws IOException {
        os.writeDouble(v);
    }

    public void writeFloat(float v) throws IOException {
        os.writeFloat(v);
    }

    public void writeBoolean(boolean v) throws IOException {
        os.writeBoolean(v);
    }

    public void write(byte[] bytes) throws IOException {
        os.write(bytes);
    }

    public void writeDate(Date date) throws IOException {
        write(date.getDay());
        write(date.getMonth());
        writeShort(date.getYear());
    }

    public void writeColor(Color color) throws IOException {
        write(color.getRed());
        write(color.getGreen());
        write(color.getBlue());
    }

    public void writeLocation(Location location) throws IOException {
        writeLocation(location.getLatitude(), location.getLongitude());
    }

    public void writeLocation(double lat, double lon) throws IOException {
        writeInt((int) (lat * 1e6));
        writeInt((int) (lon * 1e6));
    }


    public void writeString(String s) throws IOException {
        if (s.contains("\0")) {
            throw new IllegalStateException("String '"+s+"' contains a NUL char so NUL-terminated string can not be safely written");
        }

        s += '\0';
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);

        os.write(bytes);
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
}
