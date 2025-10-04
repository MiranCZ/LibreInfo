package com.example.mhdstuff.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.devtest.LineListActivity;

public class SettingsActivity extends BaseActivity {


    public SettingsActivity() {
        super("Nastavení");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_settings);

        Button devSettings = findViewById(R.id.open_dev_settings);

        devSettings.setOnClickListener(view -> {
            startActivity(LineListActivity.class);
        });
    }
}
