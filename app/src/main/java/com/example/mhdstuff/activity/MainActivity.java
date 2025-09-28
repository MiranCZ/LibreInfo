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

        buttonOpenSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        // Add OnClickListeners for other buttons as needed
        // For example:
         buttonOpenNews.setOnClickListener(v -> {
             Intent intent = new Intent(MainActivity.this, NewsActivity.class);
             startActivity(intent);
         });


        buttonOpenVehicleMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VehicleMapActivity.class);
            startActivity(intent);
        });

        buttonOpenVehiclesList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VehiclesListActivity.class);
            startActivity(intent);
        });


        buttonOpenSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

         buttonOpenDiversions.setOnClickListener(view -> {
             Intent intent = new Intent(MainActivity.this, DiversionsActivity.class);
             startActivity(intent);

         });

        buttonOpenEvents.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EventsActivity.class);
            startActivity(intent);
        });
    }
}