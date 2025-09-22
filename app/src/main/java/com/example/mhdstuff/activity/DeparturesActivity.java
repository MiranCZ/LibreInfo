package com.example.mhdstuff.activity;

import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.data.StopDataHolder;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Stop;
import com.example.mhdstuff.parsing.types.departure.Departure;
import com.example.mhdstuff.parsing.types.departure.Departures;
import com.example.mhdstuff.util.request.RequestHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class DeparturesActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_departures);

        Stop stop = StopDataHolder.getStop();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(stop.name());
        }

        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();

            Departures departures = Departures.parse(RequestHelper.getDepartures(stop.id()), storage.lineStorage());

            LinearLayout layout = findViewById(R.id.departure_items);

            Context context = this;
            createEntries(departures, layout, context);
        }).start();
    }

    private void createEntries(Departures departures, LinearLayout layout, Context context) {
        List<Departure> departureList = new ArrayList<>(departures.departures());
        AtomicInteger index = new AtomicInteger(1);

        // incremental loading of elements to increase speed of opening the screen; the effect is practically unnoticeable
        final int viewsPerFrame = 2;

        while (!departureList.isEmpty()) {
            CountDownLatch latch = new CountDownLatch(1);

            runOnUiThread(() -> {
                TextView message = findViewById(R.id.departure_message);
                if (!departures.message().isBlank()) {
                    message.setVisibility(TextView.VISIBLE);
                    message.setText(departures.message());
                }

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
