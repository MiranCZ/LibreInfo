package me.miran.parsing;

import me.miran.util.CheckedOutputStream;
import me.miran.util.Pair;
import me.miran.util.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ApiParser extends Parser {

    static final String START_TEXT = "Linka/CVlaku = trip_id: ";

    public ApiParser(){
        subscribeTransformer("api.txt", "api", this::parseAndWrite);
    }

    protected void parseAndWrite(InputStream input, CheckedOutputStream output) throws Exception {
        Scanner scanner = new Scanner(Utils.detectCharset(input));


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

            int tripId = Integer.parseInt(parts[1].strip());

            result.add(new Pair<>(tripId, ((lineId<<16) | routeId)));
        }

        output.writeInt(result.size());
        for (Pair<Integer, Integer> pair : result) {
            output.writeInt(pair.left());
            output.writeInt(pair.right());
        }
    }

}
