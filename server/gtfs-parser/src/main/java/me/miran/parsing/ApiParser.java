package me.miran.parsing;

import me.miran.util.CheckedOutputStream;
import me.miran.util.IdStorage;
import me.miran.util.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ApiParser extends Parser {

    private static final String START_TEXT = "Linka/CVlaku = trip_id: ";
    private static final int SNIFF_LIMIT = 8192;

    public ApiParser(){
        subscribeTransformer("api.txt", "api", this::parseAndWrite);
    }

    protected void parseAndWrite(InputStream input, CheckedOutputStream output) throws Exception {
        Scanner scanner = new Scanner(detectCharset(input));


        List<Pair<Integer, Integer>> result = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (!line.startsWith(START_TEXT)) {
                System.out.println("Invalid line: "+line);
                continue;
            }

            line = line.substring(START_TEXT.length());

            String[] parts = line.split("=");

            String[] lineIdParts = parts[0].split("/");

            int lineId = Integer.parseInt(lineIdParts[0].strip());
            int routeId = Integer.parseInt(lineIdParts[1].strip());

            int tripId = IdStorage.TRIP.getId(Integer.parseInt(parts[1].strip()));

            result.add(new Pair<>(tripId, ((lineId<<16) | routeId)));
        }

        output.writeInt(result.size());
        for (Pair<Integer, Integer> pair : result) {
            output.writeInt(pair.left());
            output.writeInt(pair.right());
        }
    }

    // sometimes the file is arbitrarily in a different encoding (OBVIOUSLY)
    // thanks Claude
    private static Reader detectCharset(InputStream in) throws IOException {
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
