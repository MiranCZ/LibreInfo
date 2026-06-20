package me.miran.libreinfo.activity.devtest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import me.miran.libreinfo.R;
import me.miran.libreinfo.activity.base.BaseActivity;
import me.miran.libreinfo.parsing.storage.manager.AppContainer;
import me.miran.libreinfo.parsing.storage.manager.IdStorage;
import me.miran.libreinfo.parsing.types.stop.Stop;
import me.miran.libreinfo.parsing.types.stop.StopId;
import me.miran.libreinfo.util.OfflineDepartures;

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
                IdStorage storage = AppContainer.INSTANCE.getStorageProvider().getBlocking(IdStorage.class);

                long ms = System.currentTimeMillis();

                Map<Integer, Long> took = new HashMap<>();

                long total = storage.stopStorage().getAllStops().size()* 3L;
                long processed = 0;

                for (int i = 0; i < 3; i++) {
                    for (Stop stop : storage.stopStorage().getAllStops()) {
                        long startMs = System.currentTimeMillis();
                        var result = OfflineDepartures.getOffline(storage, stop.id.internal());
                        long tookMs = System.currentTimeMillis()-startMs;

                        if (!result.isEmpty()) {
                            took.put(stop.id.internal(), Math.min(took.getOrDefault(stop.id, Long.MAX_VALUE), tookMs));
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

                String minStopS = storage.stopStorage().getStop(StopId.internal(minStop)).name;
                String maxStopS = storage.stopStorage().getStop(StopId.internal(maxStop)).name;

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
