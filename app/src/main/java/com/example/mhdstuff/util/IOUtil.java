package com.example.mhdstuff.util;

import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {


    public static byte[] readAllBytes(InputStream stream) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return stream.readAllBytes();
        } else {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = stream.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
                return out.toByteArray();
            }
        }
    }

    public static byte[] readNBytes(InputStream stream, int size) throws IOException {
        byte[] bytes = new byte[size];
        int read = stream.read(bytes);

        if (read != size) throw new EOFException();
        return bytes;
    }

}
