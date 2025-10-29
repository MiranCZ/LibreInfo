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
import com.example.mhdstuff.exception.AppException;
import com.example.mhdstuff.exception.RequestException;
import com.example.mhdstuff.parsing.storage.CalendarStorage;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.RouteStop;
import com.example.mhdstuff.parsing.types.Stop;
import com.example.mhdstuff.parsing.types.Time;
import com.example.mhdstuff.parsing.types.Trip;
import com.example.mhdstuff.parsing.types.Vehicle;
import com.example.mhdstuff.parsing.types.VehicleTripInfo;
import com.example.mhdstuff.parsing.types.departure.VehicleInfo;
import com.example.mhdstuff.util.request.RequestHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

            var res = storage.apiStorage().getLineIdAndRoute(tripId);

            VehicleTripInfo vehicleInfo = VehicleTripInfo.NONE;
            try {
                vehicleInfo = VehicleTripInfo.parse(RequestHelper.getVehicleInfo(res.left(), res.right()));
            } catch (RequestException e) {
                runOnUiThread(() -> e.showError(this, AppException.NotificationType.SNACK_BAR));
            }

            VehicleTripInfo finalVehicleInfo = vehicleInfo;
            runOnUiThread(() -> create(storage, tripId, lineIcon, finalVehicleInfo));
        }).start();

    }

    // FIXME delays not displaying for all trips in multi-trip routes; further stops are ignored
    private void create(IdStorage storage, int tripId, FrameLayout lineIcon, VehicleTripInfo vehicleInfo) {
        int delay = getIntent().getIntExtra("delay", -1);
        int highlightedStopId = getIntent().getIntExtra("stopId", -1);

        var res = storage.apiStorage().getLineIdAndRoute(tripId);
        int lineId = res.left();

        lineIcon.addView(storage.lineStorage().getAlias(lineId).createLineIconView(lineIcon, this));

        TextView heading = findViewById(R.id.vehicle_heading);


        if (tripId == -1) {
            heading.setText("Unknown final stop");
            return;
        }
        TextView vehicleIdInfo = findViewById(R.id.vehicle_id_info);
        TextView routeInfo = findViewById(R.id.vehicle_route_info);

        int vehicleId = getIntent().getIntExtra("vehicleId", -1);

        if (vehicleId != -1) {
            vehicleIdInfo.setText("Vůz "+vehicleId);
        } else {
            vehicleIdInfo.setVisibility(View.GONE);
        }

        Trip trip = storage.tripStorage().getTrips()[tripId];
        String headsign = storage.tripStorage().getTripHeadsign(trip);

        String routeInfoText = "Trasa " + res.left() + "/" + res.right();


        RouteStop[] stops = trip.getRouteStops(storage.routeStopStorage());

        if (trip.blockId() != -1) {
            List<Trip> neighbors = new ArrayList<>(storage.tripStorage().getTripsForBlock(trip.blockId()));

            neighbors.removeIf(t ->  !storage.calendarStorage().available(CalendarStorage.Date.now(), t.serviceId()));

            neighbors.sort(Comparator.comparing(t -> storage.routeStopStorage().getRouteStop(t.startPos()).departure()));

            headsign = storage.tripStorage().getHeadsignForTripList(neighbors, storage);

            routeInfoText = "Trasa ";


            List<RouteStop> stopsList = new ArrayList<>();

            for (int i = 0; i < neighbors.size(); i++) {
                Trip neighbor = neighbors.get(i);
                var info = storage.apiStorage().getLineIdAndRoute(neighbor.id());

                if (i != 0) {
                    routeInfoText += " => ";
                }
                routeInfoText += info.left() + "/" + info.right();

                RouteStop[] routeStops = neighbor.getRouteStops(storage.routeStopStorage());
                for (int j = 0; j < routeStops.length; j++) {
                    RouteStop routeStop = routeStops[j];

                    if (i != 0 && j == 0 && stopsList.get(stopsList.size()-1).stopId() == routeStop.stopId()) continue;
                    stopsList.add(routeStop);
                }
            }

            stops = stopsList.toArray(new RouteStop[0]);
        }

        heading.setText(headsign);
        routeInfo.setText(routeInfoText);


        TextView delayText = findViewById(R.id.vehicle_delay_info);

        if (delay == -1) {
            delayText.setVisibility(View.GONE);
        } else {
            delayText.setVisibility(View.VISIBLE);

            SpannableString span;
            if (delay > 0) {
                span = new SpannableString(delay + " min");
            } else {
                span = new SpannableString("Včas");
            }
            span.setSpan(new ForegroundColorSpan(Vehicle.getDelayColor(delay)), 0, span.length(), 0);
            delayText.setText(span);
        }


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

            // TODO fix vehicles waiting at stops in the API
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
