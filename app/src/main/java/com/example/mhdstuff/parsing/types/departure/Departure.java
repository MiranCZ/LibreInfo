package com.example.mhdstuff.parsing.types.departure;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mhdstuff.R;
import com.example.mhdstuff.parsing.storage.LineStorage;
import com.example.mhdstuff.parsing.types.TypeHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public record Departure(int stopID, int postID, String name, List<DepartureEntry> entries) {

    public static List<Departure> parseDepartures(JsonArray array, LineStorage lineStorage) {
        return TypeHelper.parseList(array, (o) -> parse(o, lineStorage));
    }

    public static Departure parse(JsonObject obj, LineStorage lineStorage) {
        if (obj == null) return null;

        int stopID = obj.get("StopID").getAsInt();
        int postID = obj.get("PostID").getAsInt();
        String name = obj.get("Name").getAsString();

        List<DepartureEntry> entries = DepartureEntry.parseEntriesList(
                obj.get("Departures").getAsJsonArray(), lineStorage
        );

        return new Departure(stopID, postID, name, entries);
    }

    public View createDepartureView(ViewGroup parent, Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.departure_layout, parent , false);

        TextView title = view.findViewById(R.id.departure_title);
        title.setText(name);

        LinearLayout items = view.findViewById(R.id.departure_items);

        int index = 0;
        for (DepartureEntry entry : entries) {
            View v = entry.createDepartureEntryView(items, context);
            items.addView(v, index++);
        }

        return view;
    }
}
