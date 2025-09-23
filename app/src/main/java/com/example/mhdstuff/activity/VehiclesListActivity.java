package com.example.mhdstuff.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mhdstuff.R;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Vehicle;
import com.example.mhdstuff.parsing.types.departure.Departure;
import com.example.mhdstuff.util.request.soap.SoapHelper;
import com.example.mhdstuff.util.request.soap.SoapSaneObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class VehiclesListActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_vehicles);


        Context context = this;
        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();

            List<Vehicle> vehicles = Vehicle.parseVehicles(SoapHelper.getVehicles(), storage);

            LinearLayout layout = findViewById(R.id.vehicle_items);
            createEntries(vehicles, layout, context, storage);
        }).start();
    }


    private void createEntries(List<Vehicle> vehicles, LinearLayout layout, Context context, IdStorage storage) {
        AtomicInteger index = new AtomicInteger(0);

        // incremental loading of elements to increase speed of opening the screen; the effect is practically unnoticeable
        final int viewsPerFrame = 15;

        List<View> views = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
             views.add(vehicle.createVehicleInfo(layout, context));
        }



        while (!vehicles.isEmpty()) {
            CountDownLatch latch = new CountDownLatch(1);

            runOnUiThread(() -> {
                for (int i = 0; i < viewsPerFrame && !views.isEmpty(); i++) {
                    Vehicle vehicle = vehicles.remove(0);
                    layout.addView(vehicle.createVehicleInfo(layout, context), index.getAndIncrement());
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
