package com.example.mhdstuff.parsing.types.departure;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.mhdstuff.R;
import com.example.mhdstuff.parsing.storage.LineStorage;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.TypeHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import java.util.List;

public record DepartureEntry(LineAlias line, String finalStop, boolean lowFloor, boolean hasAC, String timeMark, int vehicleId) {

    public static List<DepartureEntry> parseEntriesList(JsonArray array, LineStorage lineStorage) {
        return TypeHelper.parseList(array, (o) -> parse(o, lineStorage));
    }

    public static DepartureEntry parse(JsonObject obj, LineStorage lineStorage) {
        if (obj == null) return null;

        LineAlias line = LineAlias.parse(obj.get("Line").getAsString(), lineStorage);
        String finalStop = obj.get("FinalStop").getAsString();
        boolean lowFloor = obj.get("IsLowFloor").getAsBoolean();
        boolean hasAC = obj.get("HasAC").getAsBoolean();
        String timeMark = obj.get("TimeMark").getAsString();
        int vehicleID = obj.get("VehicleID").getAsInt();

        // formatting bruuuh
        if (timeMark.endsWith("min") && !timeMark.endsWith(" min")) {
            timeMark = timeMark.substring(0, timeMark.length()-3) +" min";
        }

        return new DepartureEntry(line, finalStop, lowFloor, hasAC, timeMark, vehicleID);
    }

    public View createDepartureEntryView(ViewGroup parent, Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.departure_entry_layout, parent , false);

        FrameLayout icon = view.findViewById(R.id.departure_line_icon);
        icon.addView(line.createLineIconView(icon, context),0);

        TextView heading = view.findViewById(R.id.departure_heading);
        heading.setText(finalStop);

        TextView arrival = view.findViewById(R.id.departure_arrival);
        arrival.setText(timeMark);

        if (!lowFloor) {
            view.findViewById(R.id.departure_wheelchair_icon).setVisibility(View.GONE);
        }

        return view;
    }
}
