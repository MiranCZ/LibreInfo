package io.github.mirancz.gtfs.export.writer;

import io.github.mirancz.gtfs.ParsedGtfs;
import io.github.mirancz.gtfs.export.SingleWriter;
import io.github.mirancz.gtfs.ids.RouteEntry;
import io.github.mirancz.util.CheckedOutputStream;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import static io.github.mirancz.gtfs.export.ParseUtil.parseRoute;

public class LineInfoWriter extends SingleWriter {

    public LineInfoWriter() {
        super("lines");
    }

    @Override
    protected void write(ParsedGtfs gtfs, CheckedOutputStream os) throws IOException {
        var routes = gtfs.routes();

        os.writeInt(routes.size());

        for (RouteEntry e : routes) {
            int routeId = parseRoute(e.route_id());

            String backgroundColorStr = e.route_color();
            if (backgroundColorStr.isBlank()) backgroundColorStr = "00E68C";

            String textColorStr = e.route_text_color();
            if (textColorStr.isBlank()) textColorStr = "000000";

            Color backgroundColor = parseColor(backgroundColorStr);
            Color textColor = parseColor(textColorStr);

            os.writeInt(routeId);
            os.writeString(e.route_short_name());

            writeColor(os, backgroundColor);
            writeColor(os, textColor);
        }


    }

    private static void writeColor(CheckedOutputStream os, Color backgroundColor) throws IOException {
        os.write(backgroundColor.getRed());
        os.write(backgroundColor.getGreen());
        os.write(backgroundColor.getBlue());
    }

    private static Color parseColor(String color) {
        if (color.length() != 6) {
            throw new IllegalArgumentException("Unexpected color argument: "+color);
        }

        return new Color(
                Integer.parseInt(color.substring(0,2), 16),
                Integer.parseInt(color.substring(2,4), 16),
                Integer.parseInt(color.substring(4,6), 16)
        );
    }



}
