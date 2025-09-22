package com.example.mhdstuff.parsing.types;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mhdstuff.R;
import com.example.mhdstuff.parsing.storage.LineStorage;
import com.example.mhdstuff.util.Csv;
import com.example.mhdstuff.util.CsvHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public record LineAlias(int id, String lineDisplayName, int backgroundColor, String backgroundColorStr, int textColor, String textColorStr) {

    public static List<LineAlias> parseLineAliases(JsonArray array) {
        return TypeHelper.parseList(array, LineAlias::parse);
    }

    public static LineAlias parse(JsonObject obj) {
        if (obj == null) return null;

        int id = obj.get("LineID").getAsInt();
        String displayName = obj.get("LineName").getAsString();

        String backgroundStr = obj.get("Color").getAsString();
        int background = Color.parseColor(backgroundStr);
        String textColorStr = obj.get("TextColor").getAsString();
        int textColor = Color.parseColor(textColorStr);

        return new LineAlias(id, displayName, background, backgroundStr, textColor, textColorStr);
    }

    public static LineAlias parse(String str, LineStorage storage) {
        if (str == null) return null;
        str = str.strip();

        if (TypeHelper.isInteger(str)) {
            int id = Integer.parseInt(str);

            return storage.getAlias(id);
        }

        return storage.getAlias(str);
    }

    public static List<LineAlias> parseCsv(String csvStr) {
        Csv csv = CsvHelper.parseCsvEntries(csvStr);

        List<LineAlias> result = new ArrayList<>();
        for (Csv.CsvLine line : csv.getLines()) {
            try {
                result.add(parse(line));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("LineAlias", line.toString());
            }
        }
        return result;
    }

    public static LineAlias parse(Csv.CsvLine line) {
        String routeId = line.get("route_id");
        if (!routeId.startsWith("L")) throw new IllegalStateException(routeId);
        int dInd = routeId.indexOf("D");
        if (dInd == -1) throw new IllegalStateException(routeId);

        int id = Integer.parseInt(routeId.substring(1, dInd));

        String displayName = line.get("route_short_name");

        String backgroundStr = normalizeColor(line.get("route_color"));
        int background = Color.parseColor(backgroundStr);
        String textColorStr = normalizeColor(line.getOrDefault("route_text_color", "000000"));
        int textColor = Color.parseColor(textColorStr);

        return new LineAlias(id, displayName, background, backgroundStr, textColor, textColorStr);
    }

    private static String normalizeColor(String colorStr) {
        String saved = colorStr;
        colorStr = colorStr.strip();
        if (colorStr.isEmpty()) {
            return "#000000";
        }

        if (colorStr.startsWith("#")) {
            colorStr = colorStr.substring(1);
        }
        String temp = colorStr;
        while (colorStr.length() < 6) {
            colorStr = colorStr + temp;
        }
        if (colorStr.length() != 6) {
            throw new IllegalStateException("What even is this input??? "+saved);
        }

        return "#"+colorStr;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("LineID", id);

        obj.addProperty("LineName", lineDisplayName);
        obj.addProperty("Color", backgroundColorStr);
        obj.addProperty("TextColor", textColorStr);

        return obj;
    }


    public TransportLine toTransportLine() {
        return new TransportLine(id, lineDisplayName);
    }

    public View createLineIconView(ViewGroup parent, Context context) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.line_icon_layout, parent , false);
        TextView title = itemView.findViewById(R.id.line_name);
        title.setText(lineDisplayName());
        title.setTextColor(textColor());

        View view = itemView.findViewById(R.id.icon_container);
        GradientDrawable back = (GradientDrawable) view.getBackground();
        back.setColor(backgroundColor());

        // hardcoded outline around black background
        if (backgroundColor == -16777216) {
            back.setStroke(4, textColor);
        } else {
            back.setStroke(0, backgroundColor);
        }

        return itemView;
    }

}
