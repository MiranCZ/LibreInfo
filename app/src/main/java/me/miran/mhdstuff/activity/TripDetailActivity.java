package me.miran.mhdstuff.activity;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import me.miran.mhdstuff.R;
import me.miran.mhdstuff.activity.base.BaseActivity;
import me.miran.mhdstuff.exception.AppException;
import me.miran.mhdstuff.exception.RequestException;
import me.miran.mhdstuff.parsing.storage.CalendarStorage;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.types.LineAlias;
import me.miran.mhdstuff.parsing.types.RouteStop;
import me.miran.mhdstuff.parsing.types.Time;
import me.miran.mhdstuff.parsing.types.Trip;
import me.miran.mhdstuff.parsing.types.VehicleTripInfo;
import me.miran.mhdstuff.util.DelayUtil;
import me.miran.mhdstuff.util.request.RequestHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;
import java.util.function.Function;

// TODO do not restart blinking when updating
// add "updated before x seconds" timer
public class TripDetailActivity extends BaseActivity {

    private Timer timer = null;

    public TripDetailActivity() {
        super(R.string.trip, R.layout.activity_trip_info);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int tripId = getIntent().getIntExtra("tripId", -1);

        FrameLayout lineIcon = findViewById(R.id.vehicle_line_icon);

        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();

            var res = storage.apiStorage().getLineIdAndRoute(tripId);

            VehicleTripInfo vehicleInfo = VehicleTripInfo.NONE;
            try {
                vehicleInfo = VehicleTripInfo.parse(RequestHelper.getVehicleInfo(this, res.left(), res.right()));
            } catch (RequestException e) {
                runOnUiThread(() -> e.showError(this, AppException.NotificationType.SNACK_BAR));
            }

            VehicleTripInfo finalVehicleInfo = vehicleInfo;
            runOnUiThread(() -> create(storage, tripId, lineIcon, finalVehicleInfo));
        }).start();

    }

    // FIXME delays not displaying for all trips in multi-trip routes; further stops are ignored
    private void create(IdStorage storage, int tripId, FrameLayout lineIcon, VehicleTripInfo vehicleInfo) {
        int delay = vehicleInfo.delay();
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
            vehicleIdInfo.setText(getString(R.string.vehicle_number, vehicleId));
        } else {
            vehicleIdInfo.setVisibility(View.GONE);
        }

        Trip trip = storage.tripStorage().getTrips()[tripId];
        String headsign = storage.tripStorage().getTripHeadsign(trip);

        String routeInfoText = getString(R.string.trip_number, res.left(), res.right());


        RouteStop[] stops = trip.getRouteStops(storage.routeStopStorage());

        if (trip.blockId() != -1) {
            List<Trip> neighbors = new ArrayList<>(storage.tripStorage().getTripsForBlock(trip.blockId()));

            neighbors.removeIf(t ->  !storage.calendarStorage().available(CalendarStorage.Date.now(), t.serviceId()));

            neighbors.sort(Comparator.comparing(t -> storage.routeStopStorage().getRouteStop(t.startPos()).departure()));

            headsign = storage.tripStorage().getHeadsignForTripList(neighbors, storage);

            routeInfoText = getString(R.string.trip)+" ";


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

            delayText.setText(DelayUtil.getDelaySpan(this, delay));
        }


        LinearLayout view = findViewById(R.id.vehicle_stops);

        int highlightViewId = populateViews(
                (i) -> LayoutInflater.from(this).inflate(R.layout.route_stop_entry, view, false),
                (i, v) -> view.addView(v, i),
                storage,
                vehicleInfo,
                stops,
                highlightedStopId,
                lineId,
                delay
        );

        NestedScrollView scrollView = findViewById(R.id.scroll_view);

        if (highlightViewId != -1) {
            View centerView = view.getChildAt(Math.min(highlightViewId+4, view.getChildCount()-1));

            scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove listener so it only runs once
                    scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    int targetY = centerView.getTop() + centerView.getHeight() / 2;

                    int scrollTo = targetY - scrollView.getHeight() / 2;

                    scrollView.setScrollY(scrollTo);
                }
            });

        }



        BaseActivity thiz = this;
        RouteStop[] finalStops = stops;

        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        Context context = this;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                VehicleTripInfo vehicleInfo = VehicleTripInfo.NONE;
                try {
                    vehicleInfo = VehicleTripInfo.parse(RequestHelper.getVehicleInfo(context, res.left(), res.right()));
                } catch (RequestException e) {
                    runOnUiThread(() -> e.showError(thiz, AppException.NotificationType.SNACK_BAR));

                    timer.cancel();
                }

                VehicleTripInfo finalVehicleInfo = vehicleInfo;
                runOnUiThread(() -> {
                    if (finalVehicleInfo.delay() == -1) {
                        delayText.setVisibility(View.GONE);
                    } else {
                        delayText.setVisibility(View.VISIBLE);

                        delayText.setText(DelayUtil.getDelaySpan(thiz, finalVehicleInfo.delay()));
                    }

                    populateViews(view::getChildAt, (i, v) -> {
                            }, storage, finalVehicleInfo,
                            finalStops,
                            highlightedStopId,
                            lineId,
                            finalVehicleInfo.delay());
                });
            }
        }, 0, 10_000);

    }

    private int populateViews(Function<Integer, View> viewCreator, BiConsumer<Integer, View> consumer, IdStorage storage, VehicleTripInfo vehicleInfo, RouteStop[] stops, int highlightedStopId, int lineId, int delay) {
        int id = 0;
        boolean alreadyMet = true;
        int highlightViewId = -1;

        for (int i = 0; i < stops.length; i++) {
            RouteStop stop = stops[i];
            View info = viewCreator.apply(i);

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
            if (!leavingStop) {
                icon.clearAnimation();
            }

            boolean lastStop = (i+1)>= stops.length;

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

            if (stop.stopId() == highlightedStopId) {
                highlightViewId = id;
            }


            if (vehicleInfo.lastStopId() == stop.stopId()) {
                alreadyMet = false;
            }


            consumer.accept(id++, info);
        }
        return highlightViewId;
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }
}
