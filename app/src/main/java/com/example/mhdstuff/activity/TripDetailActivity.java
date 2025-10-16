package com.example.mhdstuff.activity;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.activity.data.TripDataHolder;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.RouteStop;
import com.example.mhdstuff.parsing.types.Trip;
import com.example.mhdstuff.parsing.types.Vehicle;


public class TripDetailActivity extends BaseActivity {

    public TripDetailActivity() {
        super("Trasa", R.layout.activity_trip_info);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int tripId = getIntent().getIntExtra("tripId", -1);

        FrameLayout lineIcon = findViewById(R.id.vehicle_line_icon);

        IdStorage.getInstanceOnUIThread(storage -> {
            int delay = getIntent().getIntExtra("delay", -1);

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
            for (int i = 0; i < stops.length; i++) {
                RouteStop stop = stops[i];
                View info = LayoutInflater.from(this).inflate(R.layout.route_stop_entry, view, false);

                TextView stopName = info.findViewById(R.id.stop_name);
                stopName.setText(storage.stopStorage().getStop(stop.stopId()).name);

                TextView departure = info.findViewById(R.id.departure_time);

                boolean lastStop = (i+1)>=stops.length;
                stop.setDelay(delay);
                if (delay == -1) {
                    departure.setText(stop.stopTime().formatWithoutDelay(!lastStop));
                } else {
                    departure.setText(stop.stopTime().formatColoredDelay(!lastStop));
                    delay = stop.stopTime().getLoweredDelay();
                }

                view.addView(info, id++);
            }

        }, this);
    }
}
