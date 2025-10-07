package com.example.mhdstuff.activity;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
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
import com.example.mhdstuff.util.Container;
import com.example.mhdstuff.util.OfflineDepartures;
import com.example.mhdstuff.util.request.soap.SoapHelper;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class DeparturesActivity extends BaseActivity {



    private final Stop stop;

    public DeparturesActivity() {
        super(StopDataHolder.getStop().name);
        stop = StopDataHolder.getStop();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_departures);

        Container<View> heartFullCont = new Container<>();
        View heartEmpty = addButtonIcon(R.drawable.heart_regular, v -> {
            v.setVisibility(View.GONE);
            heartFullCont.item.setVisibility(View.VISIBLE);

            stop.setFavourite(true);
        });

        View heartFull = addButtonIcon(R.drawable.heart_solid, v -> {
            heartEmpty.setVisibility(View.VISIBLE);
            v.setVisibility(View.GONE);

            stop.setFavourite(false);
        }, false);

        heartFullCont.item = heartFull;

        if (stop.isFavourite()) {
            heartEmpty.setVisibility(View.GONE);
        } else {
            heartFull.setVisibility(View.GONE);
        }

        Context context = this;
        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();

            Departures departures = Departures.parse(
                    SoapHelper.getDepartures(stop.id), SoapHelper.getVehicles(), stop.id, storage
            );

            // fallback to offline if something went wrong
            if (departures == null) {
                departures = getOffline(storage);
            }

            LinearLayout layout = findViewById(R.id.departure_items);

            createEntries(departures, layout, context);
        }).start();
    }


    private Departures getOffline(IdStorage storage) {
        return new Departures(
                "!!!!\nYOU ARE VIEWING THIS IN OFFLINE MODE!\n!!!!",
                OfflineDepartures.getOffline(storage, stop.id)
        );
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
                    layout.addView(departure.createDepartureView(this, layout, context), index.getAndIncrement());
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

    @Override
    public void onPause() {
        super.onPause();

        stop.flush();
    }
}
