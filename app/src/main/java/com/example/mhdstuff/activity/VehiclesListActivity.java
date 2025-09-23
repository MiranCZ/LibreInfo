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

    @Override
    protected RecyclerView.Adapter<?> getAdapter(Context context, IdStorage storage) {
        List<Vehicle> vehicles = Vehicle.parseVehicles(SoapHelper.getVehicles(), storage);

        return new VehicleItemAdapter(vehicles, context);
    }

}
