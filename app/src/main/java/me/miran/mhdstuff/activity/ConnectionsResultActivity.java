package me.miran.mhdstuff.activity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import me.miran.mhdstuff.R;
import me.miran.mhdstuff.activity.base.BaseActivity;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.types.LineAlias;
import me.miran.mhdstuff.parsing.types.Time;
import me.miran.mhdstuff.raptor.Path;
import me.miran.mhdstuff.raptor.PathNode;
import me.miran.mhdstuff.raptor.Raptor;

public class ConnectionsResultActivity extends BaseActivity {
    public ConnectionsResultActivity() {
        super("Spojení", R.layout.activity_connection_result);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();


            List<Path> paths = Raptor.getDepartures((short) 2924, (short) 2876);

            runOnUiThread(() -> create(storage, paths));
        }).start();
    }


    private void create(IdStorage storage, List<Path> paths) {
        LinearLayout list = findViewById(R.id.list);

        for (int i = 0; i < paths.size(); i++) {
            View view = createPathView(storage, paths.get(i), list);

            list.addView(view, i);
        }
    }

    @NonNull
    private LinearLayout createPathView(IdStorage storage, Path path, ViewGroup parent) {
        LinearLayout ll = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.connection_layout, parent, false);

        var orig = ll;
        ll = ll.findViewById(R.id.list);

        List<PathNode> nodes = path.nodes();
        for (int i = 0; i < nodes.size(); i++) {
            PathNode node = nodes.get(i);
            View view = node.createView(storage, ll, this);

            FrameLayout gradient = view.findViewById(R.id.transfer_gradient);
            if (i + 1 < nodes.size()) {
                LineAlias current = storage.lineStorage().getAlias(node.trip().lineId());
                LineAlias next = storage.lineStorage().getAlias(nodes.get(i+1).trip().lineId());

                int[] colors = {
                        Color.parseColor(current.backgroundColorStr()),
                        Color.parseColor(next.backgroundColorStr())
                };

                GradientDrawable gradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        colors
                );

                gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                gradientDrawable.setCornerRadius(20f);

                gradient.setBackground(gradientDrawable);
            } else {
                gradient.setVisibility(View.INVISIBLE);
            }

            ll.addView(view, i);
        }

        TextView routeLenText = orig.findViewById(R.id.route_length);
        routeLenText.setText(formatMins(path.getMinuteLength()));

        TextView departTime = orig.findViewById(R.id.leave_time);
        departTime.setText(formatTime(path.getDeparture()));
        return orig;
    }


    // TODO add translations
    private String formatTime(Time departure) {
        Time now = Time.now();

        int diff = departure.getMinsDiff(now);

        if (diff == 0) return "nyní";

        if (diff > 0) {
            return "za "+formatMins(diff);
        }

        return departure.format();
    }

    private String formatMins(int mins) {
        int hours = mins / 60;
        int minutes = mins % 60;

        if (hours > 0) {
            return hours + " hod " + minutes + " min";
        }

        return minutes + " min";
    }

}
