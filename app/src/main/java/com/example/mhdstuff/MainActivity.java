package com.example.mhdstuff;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mhdstuff.parsing.storage.IdStorage;

public class MainActivity extends AppCompatActivity {

    public static IdStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (storage == null) {
            new Thread(() -> {
                Log.e("MainActivity", "Creating storage...");
                storage = IdStorage.create(this);
            }).start();
        }

        setContentView(R.layout.activity_main);

        Button buttonOpenSearch = findViewById(R.id.open_departures);
        Button buttonOpenNews = findViewById(R.id.open_news); // Example
        Button buttonOption3 = findViewById(R.id.open_vehicles); // Example

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
    }
}