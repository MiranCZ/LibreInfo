package com.example.mhdstuff.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.R;
import com.example.mhdstuff.VehicleItemAdapter;
import com.example.mhdstuff.activity.listview.AbstractListViewActivity;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Location;
import com.example.mhdstuff.parsing.types.Vehicle;
import com.example.mhdstuff.parsing.types.departure.Departure;
import com.example.mhdstuff.util.request.soap.SoapHelper;
import com.example.mhdstuff.util.request.soap.SoapSaneObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class VehiclesListActivity extends AbstractListViewActivity {


    public VehiclesListActivity() {
        super("Seznam vozidel", R.layout.activity_vehicles, R.id.recycler_view);
    }

    /*@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_vehicles);


        Context context = this;
        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();

            List<Vehicle> vehicles = Vehicle.parseVehicles(SoapHelper.getVehicles(), storage);

            LinearLayout layout = findViewById(R.id.);
            createEntries(vehicles, layout, context, storage);
        }).start();
    }*/

    @Override
    protected RecyclerView.Adapter<?> getAdapter(Context context, IdStorage storage) {
        List<Vehicle> vehicles = Vehicle.parseVehicles(SoapHelper.getVehicles(), storage);


        return new VehicleItemAdapter(vehicles, context);
//        Vehicle dummy = new Vehicle(1234, 5678, 0, -1, -1, Location.NONE, 0, storage.lineStorage().getAlias(1), 0
//                , "", true, 1, storage.stopStorage().getStop(1146), storage.stopStorage().getStop(1001), Optional.empty()
//        , false, 111);
//        return new VehicleItemAdapter(List.of(dummy, dummy, dummy), context);
    }


    /*private void createEntries(List<Vehicle> vehicles, LinearLayout layout, Context context, IdStorage storage) {
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
                    View view = views.remove(0);
                    layout.addView(view, index.getAndIncrement());
                }
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }*/
}
