package me.miran.request;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class Util {

    static String readSecret(String name) {
        Path path = Path.of("/run/secrets/").resolve(name);

        try {
            String value = Files.readString(path).strip();
            if (value.isEmpty()) {
                throw new IllegalStateException("Secret at " + path + " is empty");
            }
            return value;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read secret from " + path + " (is the secret mounted?)", e);
        }
    }

    static List<Pos> decodeGooglePolyline(String line) {
        List<Pos> result = new ArrayList<>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < line.length()) {
            int[] latRes = decodeValue(line, index);
            lat += latRes[0];
            int[] lngRes = decodeValue(line, latRes[1]);
            lng += lngRes[0];
            index = lngRes[1];

            result.add(new Pos(lat / 1e5, lng / 1e5));
        }

        return result;
    }

    // returns {decoded delta, index after the value}
    private static int[] decodeValue(String line, int index) {
        int result = 0;
        int shift = 0;
        int b;

        do {
            if (index >= line.length()) {
                throw new IllegalArgumentException("Truncated polyline: " + line);
            }
            b = line.charAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
        } while (b >= 0x20);

        int delta = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
        return new int[]{delta, index};
    }

}
