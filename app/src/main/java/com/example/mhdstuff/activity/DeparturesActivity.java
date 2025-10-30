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
import com.example.mhdstuff.activity.data.DelaysDataHolder;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Post;
import com.example.mhdstuff.parsing.types.Stop;
import com.example.mhdstuff.parsing.types.departure.Departure;
import com.example.mhdstuff.parsing.types.departure.Departures;
import com.example.mhdstuff.util.Container;
import com.example.mhdstuff.util.OfflineDepartures;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class DeparturesActivity extends BaseActivity {




    public DeparturesActivity() {
        super("", R.layout.activity_departures);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Stop stop = getIntent().getParcelableExtra("stop");
        setName(stop.name);

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
        JsonObject delays = DelaysDataHolder.getDelays();

        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();

            Departures departures = getOffline(storage, delays, stop.id);

            LinearLayout layout = findViewById(R.id.departure_items);

            createEntries(departures, layout, stop.id, context, storage);
        }).start();
    }


    private Departures getOffline(IdStorage storage, JsonObject delays, int stopId) {
        return new Departures(
                "Work in progress...",
                OfflineDepartures.getOffline(storage, stopId, delays)
        );
    }

    private void createEntries(Departures departures, LinearLayout layout, int stopId, Context context, IdStorage storage) {
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

                    View departureView = departure.createDepartureView(this, layout, context);
                    Post post = storage.postStorage().getPost(stopId, departure.postID());
                    departureView.setOnClickListener(v -> startActivity(
                            DeparturePostDetailActivity.class,
                            intent -> intent.putExtra("post", post)
                    ));

                    layout.addView(departureView, index.getAndIncrement());
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
    }
}
