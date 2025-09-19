package com.example.mhdstuff.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mhdstuff.R;
import com.example.mhdstuff.parsing.storage.IdStorage;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button buttonOpenSearch = findViewById(R.id.open_departures);
        Button buttonOpenNews = findViewById(R.id.open_news); // Example
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
        buttonOpenSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

         buttonOpenDiversions.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(MainActivity.this, DiversionsActivity.class);
                 startActivity(intent);

             }
         });
    }
}