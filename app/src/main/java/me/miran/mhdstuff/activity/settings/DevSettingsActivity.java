package me.miran.mhdstuff.activity.settings;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import me.miran.mhdstuff.R;
import me.miran.mhdstuff.activity.base.BaseActivity;
import me.miran.mhdstuff.activity.devtest.DeparturePerformanceActivity;
import me.miran.mhdstuff.activity.devtest.LineListActivity;

public class DevSettingsActivity extends BaseActivity {


    public DevSettingsActivity() {
        super("Dev settings");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dev_settings);

        Button devSettings = findViewById(R.id.open_line_test);
        Button departurePerf = findViewById(R.id.open_departure_perf);

        devSettings.setOnClickListener(view -> startActivity(LineListActivity.class));
        departurePerf.setOnClickListener(view -> startActivity(DeparturePerformanceActivity.class));
    }
}
