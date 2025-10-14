package com.example.mhdstuff.activity;

import android.os.Bundle;
import android.widget.Button;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.activity.settings.SettingsActivity;

public class MainActivity extends BaseActivity {


    public MainActivity() {
        super("Dpmb stuff", R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button buttonOpenSearch = findViewById(R.id.open_departures);
        Button buttonOpenVehicleMap = findViewById(R.id.open_map);
        Button buttonOpenEvents = findViewById(R.id.open_events);
        Button buttonOpenVehiclesList = findViewById(R.id.open_vehicles);
//        Button buttonOpenNews = findViewById(R.id.open_news);
        Button buttonOpenDiversions = findViewById(R.id.open_diversions);
        Button buttonOpenSettings = findViewById(R.id.open_settings);
        Button buttonOpenAbout = findViewById(R.id.open_about);

        buttonOpenSearch.setOnClickListener(v -> startActivity(SearchActivity.class));
//        buttonOpenNews.setOnClickListener(v -> startActivity(NewsActivity.class));
        buttonOpenVehicleMap.setOnClickListener(v -> startActivity(VehicleMapActivity.class));
        buttonOpenVehiclesList.setOnClickListener(v -> startActivity(VehiclesListActivity.class));
        buttonOpenSettings.setOnClickListener(v -> startActivity(SettingsActivity.class));
        buttonOpenDiversions.setOnClickListener(view -> startActivity(DiversionsActivity.class));
        buttonOpenEvents.setOnClickListener(view -> startActivity(EventsActivity.class));
        buttonOpenAbout.setOnClickListener(view -> startActivity(AboutActivity.class));
    }
}