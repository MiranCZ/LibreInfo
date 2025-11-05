package me.miran.mhdstuff.activity.devtest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import me.miran.mhdstuff.R;
import me.miran.mhdstuff.activity.base.BaseActivity;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.types.Stop;
import me.miran.mhdstuff.util.OfflineDepartures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeparturePerformanceActivity extends BaseActivity {

    public DeparturePerformanceActivity() {
        super("Departure performance");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_departure_performance);

        Button button = findViewById(R.id.test_start);

        TextView text = findViewById(R.id.result_text);

        button.setOnClickListener(v -> {
            text.setText("Calculating...");

            new Thread(() -> {
                IdStorage storage = IdStorage.getInstance();

                long ms = System.currentTimeMillis();

                Map<Integer, Long> took = new HashMap<>();

                long total = storage.stopStorage().getAllStops().size()* 3L;
                long processed = 0;

                for (int i = 0; i < 3; i++) {
                    for (Stop stop : storage.stopStorage().getAllStops()) {
                        long startMs = System.currentTimeMillis();
                        var result = OfflineDepartures.getOffline(storage, stop.id);
                        long tookMs = System.currentTimeMillis()-startMs;

                        if (!result.isEmpty()) {
                            took.put(stop.id, Math.min(took.getOrDefault(stop.id, Long.MAX_VALUE), tookMs));
                        }

                        processed++;
                        if (processed % 100 == 0) {
                            double percent = Math.round(processed/((double)total)*100_00)/100d;
                            runOnUiThread(() -> text.setText("Calculating... "+percent+"%"));
                        }
                    }
                }

                long msTotal = System.currentTimeMillis() - ms;

                String resultText = "Whole test took: "+msTotal+"ms\n";

                long min = Long.MAX_VALUE;
                int minStop = -1;

                long max = Long.MIN_VALUE;
                int maxStop = -1;

                double average = 0;
                List<Long> values = new ArrayList<>();

                for (var entry : took.entrySet()) {
                    long value = entry.getValue();

                    if (value > max) {
                        max = value;
                        maxStop = entry.getKey();
                    }
                    if (value < min) {
                        min = value;
                        minStop = entry.getKey();
                    }

                    average += value;
                    values.add(value);
                }
                average /= took.size();

                Collections.sort(values);

                String minStopS = storage.stopStorage().getStop(minStop).name;
                String maxStopS = storage.stopStorage().getStop(maxStop).name;

                resultText += "\nAverage: " +Math.round(average*100)/100+"ms";
                resultText += "\nMedian: " +values.get(values.size()/2)+"ms\n";
                resultText += "\nMaximum took: " +max+"ms ("+maxStopS+")";
                resultText += "\nMinimum took: " +min+"ms ("+minStopS+")";

                String finalResultText = resultText;
                runOnUiThread(() -> text.setText(finalResultText));
            }).start();
        });
    }
}
