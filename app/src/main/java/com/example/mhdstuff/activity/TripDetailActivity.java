package com.example.mhdstuff.activity;


import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.exception.RequestException;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.RouteStop;
import com.example.mhdstuff.parsing.types.Time;
import com.example.mhdstuff.parsing.types.Trip;
import com.example.mhdstuff.parsing.types.Vehicle;
import com.example.mhdstuff.parsing.types.VehicleTripInfo;
import com.example.mhdstuff.util.request.RequestHelper;


public class TripDetailActivity extends BaseActivity {

    public TripDetailActivity() {
        super("Trasa", R.layout.activity_trip_info);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int tripId = getIntent().getIntExtra("tripId", -1);
        int vehicleId = getIntent().getIntExtra("vehicleId", -1);

        FrameLayout lineIcon = findViewById(R.id.vehicle_line_icon);

        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();

            VehicleTripInfo vehicleInfo = VehicleTripInfo.NONE;
            try {
                vehicleInfo = VehicleTripInfo.parse(RequestHelper.getVehicleInfo(vehicleId));
            } catch (RequestException e) {
                runOnUiThread(() -> e.showErrPopup(this));
            }

            VehicleTripInfo finalVehicleInfo = vehicleInfo;
            runOnUiThread(() -> create(storage, tripId, lineIcon, finalVehicleInfo));
        }).start();

    }

    private void create(IdStorage storage, int tripId, FrameLayout lineIcon, VehicleTripInfo vehicleInfo) {
        int delay = getIntent().getIntExtra("delay", -1);
        int highlightedStopId = getIntent().getIntExtra("stopId", -1);

        // TODO check correctness
        var res = storage.apiStorage().getLineIdAndRoute(tripId);
        int lineId = res.left();

        lineIcon.addView(storage.lineStorage().getAlias(lineId).createLineIconView(lineIcon, this));

        TextView heading = findViewById(R.id.vehicle_heading);

        if (tripId == -1) {
            heading.setText("Unknown final stop");
            return;
        }

        Trip trip = storage.tripStorage().getTrips()[tripId];
        heading.setText(storage.tripStorage().getTripHeadsign(trip));

        TextView delayText = findViewById(R.id.vehicle_delay_info);

        if (delay == -1) {
            delayText.setVisibility(View.GONE);
        } else {
            delayText.setVisibility(View.VISIBLE);

            if (delay > 0) {
                String delayString = "Zpoždění";

                SpannableString span = new SpannableString(delayString + " " + delay + " min");
                span.setSpan(new ForegroundColorSpan(Vehicle.getDelayColor(delay)), delayString.length(), span.length(), 0);
                delayText.setText(span);
            } else {
                SpannableString span = new SpannableString("Včas");
                span.setSpan(new ForegroundColorSpan(Vehicle.getDelayColor(delay)), 0, span.length(), 0);
                delayText.setText(span);
            }
        }

        TextView routeInfo = findViewById(R.id.vehicle_route_info);

        routeInfo.setText("Trasa " + res.left() + "/" + res.right());

        RouteStop[] stops = trip.getRouteStops(storage.routeStopStorage());

        LinearLayout view = findViewById(R.id.vehicle_stops);

        int id = 0;
        boolean alreadyMet = true;

        for (int i = 0; i < stops.length; i++) {
            RouteStop stop = stops[i];
            View info = LayoutInflater.from(this).inflate(R.layout.route_stop_entry, view, false);

            if (vehicleInfo == VehicleTripInfo.NONE && !stop.departure().isBefore(Time.now())) {
                alreadyMet = false;
            }

            TextView stopName = info.findViewById(R.id.stop_name);

            SpannableString span = new SpannableString(storage.stopStorage().getStop(stop.stopId()).name);

            if (highlightedStopId == stop.stopId()) {
                Typeface font = ResourcesCompat.getFont(this, R.font.roboto_black);
                if (font != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        span.setSpan(new TypefaceSpan(font), 0, span.length(), 0);
                    }
                }
            }

            stopName.setText(span);

            TextView departure = info.findViewById(R.id.departure_time);

            ImageView icon = info.findViewById(R.id.circle_icon);

            LineAlias alias = storage.lineStorage().getAlias(lineId);
            icon.setColorFilter(alias.backgroundColor());

            long currentSec = System.currentTimeMillis()/1000;

            boolean leavingStop = stop.stopId() == vehicleInfo.lastStopId() && (currentSec- vehicleInfo.lastUpdate()) < 20;

            if (leavingStop) {
                AlphaAnimation blink = new AlphaAnimation(0.2f, 0.9f);
                blink.setDuration(750);
                blink.setStartOffset(20);
                blink.setRepeatMode(Animation.REVERSE);
                blink.setRepeatCount(Animation.INFINITE);

                icon.startAnimation(blink);
            } else if (highlightedStopId == stop.stopId()) {
                icon.setAlpha(1f);
            } else if (alreadyMet) {
                icon.setAlpha(0.25f);
            } else {
                icon.setAlpha(0.5f);
            }

            boolean lastStop = (i+1)>=stops.length;

            int currentDelay = delay;
            if (alreadyMet) {
                currentDelay = vehicleInfo.previousStopDelays().getOrDefault(stop.stopId(), -1);
            }

            stop.setDelay(currentDelay);

            if (currentDelay == -1) {
                departure.setText(stop.stopTime().formatWithoutDelay(!lastStop));
            } else {
                departure.setText(stop.stopTime().formatColoredDelay(!lastStop));

                if (!alreadyMet) {
                    delay = stop.stopTime().getLoweredDelay();
                }
            }
            if (alreadyMet && !leavingStop) {
                stopName.setAlpha(0.4f);
                departure.setAlpha(0.4f);
            }

            view.addView(info, id++);

            if (vehicleInfo.lastStopId() == stop.stopId()) {
                alreadyMet = false;
            }
        }
    }
}
