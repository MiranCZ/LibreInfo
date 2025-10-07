package com.example.mhdstuff.activity.settings;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;

public class SettingsActivity extends BaseActivity {


    public SettingsActivity() {
        super("Nastavení");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_settings);

        Button lineTest = findViewById(R.id.open_dev_settings);

        lineTest.setOnClickListener(view -> {
            startActivity(DevSettingsActivity.class);
        });
    }
}
