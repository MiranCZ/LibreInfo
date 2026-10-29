package me.miran.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Utils {

    public static StopInfo parseStop(String stopUID) {
        if (!stopUID.startsWith("U")) {
            throw new IllegalStateException("Invalid stop UID: "+stopUID);
        }

        // format is U{stopId}Z{postId} or U{stopId}N{postId}

        int zInd = stopUID.indexOf("Z");
        int nInd = stopUID.indexOf("N");
        int ind = Math.max(zInd, nInd);

        if (ind == -1) throw new IllegalArgumentException("Invalid stop UID: " + stopUID);

        int stopId = Integer.parseInt(stopUID.substring(1, ind));
        int postId = Integer.parseInt(stopUID.substring(ind + 1));

        // the gtfs is rewritten (GtfsRewriter) before parsing, so the stop number is already mapped
        if (stopId > Short.MAX_VALUE || postId > Short.MAX_VALUE) {
            throw new IllegalStateException();
        }

        return new StopInfo((short) stopId, (short) postId);
    }

    private static final int SNIFF_LIMIT = 8192;

    // sometimes the file is arbitrarily in a different encoding (OBVIOUSLY)
    // thanks Claude
    public static Reader detectCharset(InputStream in) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in, SNIFF_LIMIT * 2);
        bin.mark(SNIFF_LIMIT);

        byte[] buf = new byte[SNIFF_LIMIT];
        int n = 0;
        while (n < buf.length) {
            int r = bin.read(buf, n, buf.length - n);
            if (r < 0) break;
            n += r;
        }
        bin.reset();

        // 1. explicit BOM
        if (n >= 3 && (buf[0] & 0xFF) == 0xEF && (buf[1] & 0xFF) == 0xBB && (buf[2] & 0xFF) == 0xBF) {
            bin.skipNBytes(3);
            return new InputStreamReader(bin, StandardCharsets.UTF_8);
        }
        if (n >= 2 && (buf[0] & 0xFF) == 0xFF && (buf[1] & 0xFF) == 0xFE) {
            bin.skipNBytes(2);
            return new InputStreamReader(bin, StandardCharsets.UTF_16LE);
        }
        if (n >= 2 && (buf[0] & 0xFF) == 0xFE && (buf[1] & 0xFF) == 0xFF) {
            bin.skipNBytes(2);
            return new InputStreamReader(bin, StandardCharsets.UTF_16BE);
        }

        // 2. no BOM -> count NUL bytes by position
        int evenZeros = 0, oddZeros = 0;
        for (int i = 0; i < n; i++) {
            if (buf[i] == 0) {
                if ((i & 1) == 0) evenZeros++; else oddZeros++;
            }
        }
        if (evenZeros + oddZeros > n / 8) {
            // 00 'L' 00 'i' ... => BE ; 'L' 00 'i' 00 ... => LE
            return new InputStreamReader(bin,
                    evenZeros > oddZeros ? StandardCharsets.UTF_16BE : StandardCharsets.UTF_16LE);
        }

        return new InputStreamReader(bin, StandardCharsets.UTF_8);
    }

}
