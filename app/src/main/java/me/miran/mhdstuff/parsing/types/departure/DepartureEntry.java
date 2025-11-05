package me.miran.mhdstuff.parsing.types.departure;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.miran.mhdstuff.R;
import me.miran.mhdstuff.activity.TripDetailActivity;
import me.miran.mhdstuff.activity.base.BaseActivity;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.types.LineAlias;
import me.miran.mhdstuff.parsing.types.TimeMark;
import me.miran.mhdstuff.parsing.types.Vehicle;
import me.miran.mhdstuff.util.request.soap.SoapSaneObject;


import java.util.Map;

public record DepartureEntry(LineAlias line, String finalStop, int stopId, int postID, boolean lowFloor, TimeMark timeMark,
                             int tripId, VehicleInfo vehicleInfo) {

    public static DepartureEntry parse(SoapSaneObject obj, Map<Long, Vehicle> vehicleMap, IdStorage storage) {
        if (obj == null) return null;

        LineAlias line = LineAlias.parse(obj.getString("LineName"), storage.lineStorage());
        String finalStop = obj.getString("FinalStation");
        int postID = obj.getInt("PostID");

        boolean lowFloor = obj.getBoolean("IsBarrierLess");
        TimeMark timeMark = null;// TimeMark.parse(obj.getString("TimeMark")); // TODO make this into an object with DriveOrderSign

        int connectionId = obj.getInt("ConnectionID");
        long key = ((long) connectionId <<32) | ((long)line.id());

//        Optional<VehicleInfo> info;

        VehicleInfo info = new VehicleInfo();
        if (vehicleMap.containsKey(key)) {
            Vehicle vehicle = vehicleMap.get(key);

            info.setId(vehicle.id());
            info.setDelay(vehicle.delay());
        }

        return new DepartureEntry(line, finalStop, 0, postID , lowFloor, timeMark,-1, info);
    }

    public View createDepartureEntryView(BaseActivity activity, ViewGroup parent, Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.departure_entry_layout, parent , false);

        populateDepartureViewEntry(activity, context, view, true);

        return view;
    }

    public void populateDepartureViewEntry(BaseActivity activity, Context context, View view, boolean showDelay) {
        FrameLayout icon = view.findViewById(R.id.departure_line_icon);
        icon.removeAllViews();
        icon.addView(line.createLineIconView(icon, context),0);

        TextView heading = view.findViewById(R.id.departure_heading);
        heading.setText(finalStop);

        TextView arrival = view.findViewById(R.id.departure_arrival);

        if (vehicleInfo.hasDelay() && showDelay) {
            int delay = vehicleInfo().delay();
            int color = vehicleInfo.getDelayColor();

            timeMark.stopTime().setDelay(delay);
            String arrivalText = timeMark.getFormattedString(30, true);

            String delayStr = "";
            if (delay > 0) {
                delayStr = " ("+delay+") ";
            }

            SpannableString spannable = new SpannableString(delayStr+ arrivalText);
            spannable.setSpan(new ForegroundColorSpan(color), 0, spannable.length(), 0);

            arrival.setText(spannable);
        } else {
            String arrivalText = timeMark.getFormattedString(30, false);

            arrival.setText(arrivalText);
        }

        view.setOnClickListener(
                v -> activity.startActivity(TripDetailActivity.class, intent -> {

                    if (vehicleInfo.hasDelay()) {
                        intent.putExtra("delay", vehicleInfo.delay());
                    }
                    if (vehicleInfo.hasId()) {
                        intent.putExtra("vehicleId", vehicleInfo.id());
                    }
                    intent.putExtra("stopId", stopId);
                    intent.putExtra("tripId", tripId);
                })
        );

        if (timeMark.leaving()) {
            AlphaAnimation blink = new AlphaAnimation(0.0f, 1.0f);
            blink.setDuration(500);
            blink.setStartOffset(20);
            blink.setRepeatMode(Animation.REVERSE);
            blink.setRepeatCount(Animation.INFINITE);

            arrival.startAnimation(blink);
        }

        if (lowFloor) {
            view.findViewById(R.id.departure_wheelchair_icon).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.departure_wheelchair_icon).setVisibility(View.GONE);
        }
    }
}
