package com.example.mhdstuff.activity;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.data.StopDataHolder;
import com.example.mhdstuff.parsing.storage.CalendarStorage;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.RouteStop;
import com.example.mhdstuff.parsing.types.Stop;
import com.example.mhdstuff.parsing.types.Time;
import com.example.mhdstuff.parsing.types.TimeMark;
import com.example.mhdstuff.parsing.types.Trip;
import com.example.mhdstuff.parsing.types.departure.Departure;
import com.example.mhdstuff.parsing.types.departure.DepartureEntry;
import com.example.mhdstuff.parsing.types.departure.Departures;
import com.example.mhdstuff.util.request.RequestHelper;
import com.example.mhdstuff.util.request.soap.SoapHelper;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class DeparturesActivity extends BaseActivity {



    private final Stop stop;

    public DeparturesActivity() {
        super(StopDataHolder.getStop().name());
        stop = StopDataHolder.getStop();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_departures);

        Context context = this;
        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();

            Departures departures = Departures.parse(SoapHelper.getDepartures(stop.id()), stop.id(), storage);

            // fallback to offline if something went wrong
            if (departures == null) {
                departures = getOffline(storage);
            }

            LinearLayout layout = findViewById(R.id.departure_items);

            createEntries(departures, layout, context);
        }).start();
    }


    private Departures getOffline(IdStorage storage) {
        long ms = System.currentTimeMillis();
        RouteStop[] stops = storage.routeStopStorage().getRouteStopsParsed(stop.id());
        System.out.println("Calculating stops took "+(System.currentTimeMillis()-ms)+"ms");

        CalendarStorage calendarStorage = storage.calendarStorage();

        Map<Short, List<RouteStop>> postToStop = new HashMap<>();

        for (RouteStop stop : stops) {
            postToStop.computeIfAbsent(stop.postId(), k -> new ArrayList<>()).add(stop);
        }

        List<Departure> result = new ArrayList<>();

        for (Map.Entry<Short, List<RouteStop>> entry : postToStop.entrySet()) {
            int postId = entry.getKey();

            List<DepartureEntry> departureEntries = new ArrayList<>();

            List<RouteStop> entries = entry.getValue();

            Time now = Time.now();

            int ind = 0;

            entries.sort(Comparator.comparing(RouteStop::departure));


            Set<RouteStop> found = new HashSet<>();
            for (RouteStop stop : entries) {
                if (found.contains(stop)) continue;
                found.add(stop);

                if (ind > 4) break;
                if (now.compareTo(stop.departure()) <= 0) {
                    Trip trip = storage.tripStorage().getTrips()[stop.tripId()];;
                    String heading = storage.tripStorage().getTripHeadsign(trip);

                    if (!calendarStorage.available(trip.serviceId())) continue;

                    TimeMark timeMark = new TimeMark(
                            LocalTime.now().plusMinutes(stop.departure().getMinsDiff(Time.now())),
                            false
                    );
                    departureEntries.add(new DepartureEntry(
                            storage.lineStorage().getAlias(trip.lineId()),
                            heading,
                            postId,
                            false, // FIXME the trips might have that info actually
                            timeMark
                    ));
                    ind++;
                }
            }

            if (!departureEntries.isEmpty()) {
                Departure departure = new Departure(
                        postId,
                        storage.postStorage().getPost(stop.id(), postId).name(),
                        departureEntries
                );
                result.add(departure);
            }
        }
        result.sort(Comparator.comparingInt(Departure::postID));

        return new Departures("!!!!\nYOU ARE VIEWING THIS IN OFFLINE MODE!\n!!!!", result);
    }

    private void createEntries(Departures departures, LinearLayout layout, Context context) {
        List<Departure> departureList = new ArrayList<>(departures.departures());
        AtomicInteger index = new AtomicInteger(1);

        // incremental loading of elements to increase speed of opening the screen; the effect is practically unnoticeable
        final int viewsPerFrame = 2;

        runOnUiThread(() -> {
            TextView message = findViewById(R.id.departure_message);
            if (departureList.isEmpty()) {
                message.setVisibility(TextView.VISIBLE);
                message.setText("Nebyly nalezeny žádné odjezdy...");

                ((GradientDrawable)message.getBackground()).setColor(ContextCompat.getColor(this, R.color.widget_background));
            } else if (!departures.message().isBlank()) {
                message.setVisibility(TextView.VISIBLE);
                message.setText(departures.message());
            }
        });

        while (!departureList.isEmpty()) {
            CountDownLatch latch = new CountDownLatch(1);

            runOnUiThread(() -> {
                for (int i = 0; i < viewsPerFrame && !departureList.isEmpty(); i++) {
                    Departure departure = departureList.remove(0);
                    layout.addView(departure.createDepartureView(layout, context), index.getAndIncrement());
                }
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
