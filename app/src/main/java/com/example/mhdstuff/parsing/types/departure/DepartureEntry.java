package com.example.mhdstuff.parsing.types.departure;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.mhdstuff.R;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.storage.LineStorage;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.TimeMark;
import com.example.mhdstuff.parsing.types.TypeHelper;
import com.example.mhdstuff.util.request.soap.SoapSaneObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import java.util.List;

public record DepartureEntry(LineAlias line, String finalStop, int postID, boolean lowFloor, TimeMark timeMark) {

    public static DepartureEntry parse(SoapSaneObject obj, IdStorage storage) {
        if (obj == null) return null;

        LineAlias line = LineAlias.parse(obj.getString("LineName"), storage.lineStorage());
        String finalStop = obj.getString("FinalStation");
        int postID = obj.getInt("PostID");

        boolean lowFloor = obj.getBoolean("IsBarrierLess");
        TimeMark timeMark = TimeMark.parse(obj.getString("TimeMark")); // TODO make this into an object with DriveOrderSign

        return new DepartureEntry(line, finalStop, postID , lowFloor, timeMark);
    }

    public View createDepartureEntryView(ViewGroup parent, Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.departure_entry_layout, parent , false);

        FrameLayout icon = view.findViewById(R.id.departure_line_icon);
        icon.addView(line.createLineIconView(icon, context),0);

        TextView heading = view.findViewById(R.id.departure_heading);
        heading.setText(finalStop);

        TextView arrival = view.findViewById(R.id.departure_arrival);
        arrival.setText(timeMark.getFormattedString(60));

        if (timeMark.isLeaving()) {
            AlphaAnimation blink = new AlphaAnimation(0.0f, 1.0f);
            blink.setDuration(500);
            blink.setStartOffset(20);
            blink.setRepeatMode(Animation.REVERSE);
            blink.setRepeatCount(Animation.INFINITE);

            arrival.startAnimation(blink);
        }

        if (!lowFloor) {
            view.findViewById(R.id.departure_wheelchair_icon).setVisibility(View.GONE);
        }

        return view;
    }
}
