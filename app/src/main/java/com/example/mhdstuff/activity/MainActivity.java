package com.example.mhdstuff.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mhdstuff.R;
import com.example.mhdstuff.parsing.storage.IdStorage;

public class MainActivity extends BaseActivity {


    public MainActivity() {
        super("Dpmb stuff");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button buttonOpenSearch = findViewById(R.id.open_departures);
        Button buttonOpenVehicleMap = findViewById(R.id.open_map);
        Button buttonOpenEvents = findViewById(R.id.open_events);
        Button buttonOpenVehiclesList = findViewById(R.id.open_vehicles);
        Button buttonOpenNews = findViewById(R.id.open_news);
        Button buttonOpenDiversions = findViewById(R.id.open_diversions);
        Button buttonOpenSettings = findViewById(R.id.open_settings);

        buttonOpenSearch.setOnClickListener(v -> startActivity(SearchActivity.class));
        buttonOpenNews.setOnClickListener(v -> startActivity(NewsActivity.class));
        buttonOpenVehicleMap.setOnClickListener(v -> startActivity(VehicleMapActivity.class));
        buttonOpenVehiclesList.setOnClickListener(v -> startActivity(VehiclesListActivity.class));
        buttonOpenSettings.setOnClickListener(v -> startActivity(SettingsActivity.class));
        buttonOpenDiversions.setOnClickListener(view -> startActivity(DiversionsActivity.class));
        buttonOpenEvents.setOnClickListener(view -> startActivity(EventsActivity.class));
    }
}