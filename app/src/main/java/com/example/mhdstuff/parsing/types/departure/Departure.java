package com.example.mhdstuff.parsing.types.departure;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.BaseActivity;

import java.util.List;

public record Departure(int postID, String name, List<DepartureEntry> entries) {

    public View createDepartureView(BaseActivity activity, ViewGroup parent, Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.departure_layout, parent , false);

        TextView title = view.findViewById(R.id.departure_title);
        title.setText(name);

        LinearLayout items = view.findViewById(R.id.departure_items);

        int index = 0;
        for (DepartureEntry entry : entries) {
            View v = entry.createDepartureEntryView(activity, items, context);
            items.addView(v, index++);
        }

        return view;
    }
}
