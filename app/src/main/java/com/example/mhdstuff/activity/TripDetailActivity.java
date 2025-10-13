package com.example.mhdstuff.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.activity.data.TripDataHolder;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.RouteStop;
import com.example.mhdstuff.parsing.types.Trip;


public class TripDetailActivity extends BaseActivity {

    public TripDetailActivity() {
        super("Trasa");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trip_info);

        Trip trip = TripDataHolder.getTrip();
        FrameLayout lineIcon = findViewById(R.id.vehicle_line_icon);

        IdStorage.getInstanceOnUIThread(storage -> {
            lineIcon.addView(storage.lineStorage().getAlias(trip.lineId()).createLineIconView(lineIcon, this));

            TextView heading = findViewById(R.id.vehicle_heading);
            heading.setText(storage.tripStorage().getTripHeadsign(trip));

            TextView delay = findViewById(R.id.vehicle_delay_info);
            delay.setText("Zpoždění 7 min");

            TextView routeInfo = findViewById(R.id.vehicle_route_info);
            routeInfo.setText("Trasa 1/1234");

            RouteStop[] stops = trip.getRouteStops(storage.routeStopStorage());

            LinearLayout view = findViewById(R.id.vehicle_stops);

            int id = 0;
            for (RouteStop stop : stops) {
                View info = LayoutInflater.from(this).inflate(R.layout.route_stop_entry, view , false);

                TextView stopName = info.findViewById(R.id.stop_name);
                stopName.setText(storage.stopStorage().getStop(stop.stopId()).name);

                String depStr = stop.departure().format();

                // TODO colorize based on delay
                if (!stop.departure().equals(stop.arrival())) {
                    depStr = stop.arrival().format() + " - "+depStr;
                }
                TextView departure = info.findViewById(R.id.departure_time);
                departure.setText(depStr);

                view.addView(info, id++);

                System.out.println("ADD "+stop);
            }

        }, this);
    }
}
