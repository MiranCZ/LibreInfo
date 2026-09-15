package io.github.mirancz.libreinfo.activity.devtest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import io.github.mirancz.libreinfo.R;
import io.github.mirancz.libreinfo.activity.base.BaseActivity;
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer;
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage;
import io.github.mirancz.libreinfo.parsing.types.stop.Stop;
import io.github.mirancz.libreinfo.parsing.types.stop.StopId;
import io.github.mirancz.libreinfo.util.OfflineDepartures;

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

                long ns = System.nanoTime();

                Map<Integer, Long> took = new HashMap<>();

                long total = storage.stopStorage().getAllStops().size()* 3L;
                long processed = 0;

                for (int i = 0; i < 3; i++) {
                    for (Stop stop : storage.stopStorage().getAllStops()) {
                        long startNs = System.nanoTime();
                        var result = OfflineDepartures.getOffline(storage, stop.id.internal());
                        long tookNs = System.nanoTime()-startNs;

                        if (!result.isEmpty()) {
                            took.put(stop.id.internal(), Math.min(took.getOrDefault(stop.id.internal(), Long.MAX_VALUE), tookNs));
                        }

                        processed++;
                        if (processed % 100 == 0) {
                            double percent = Math.round(processed/((double)total)*100_00)/100d;
                            runOnUiThread(() -> text.setText("Calculating... "+percent+"%"));
                        }
                    }
                }

                long nsTotal = System.nanoTime() - ns;

                String resultText = "Whole test took: "+formatNs(nsTotal)+"ms\n";

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

                resultText += "\nAverage: " +formatNs(Math.round(average*100)/100)+"ms";
                resultText += "\nMedian: " +formatNs(values.get(values.size()/2))+"ms\n";
                resultText += "\nMaximum took: " +formatNs(max)+"ms ("+maxStopS+")";
                resultText += "\nMinimum took: " +formatNs(min)+"ms ("+minStopS+")";

                String finalResultText = resultText;
                runOnUiThread(() -> text.setText(finalResultText));
            }).start();
        });
    }

    private static String formatNs(long ns) {
        return String.format("%.2f", (ns/1_000_000d));
    }

}
