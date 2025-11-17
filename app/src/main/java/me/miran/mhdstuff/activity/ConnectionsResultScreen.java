package me.miran.mhdstuff.activity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.List;

import me.miran.mhdstuff.R;
import me.miran.mhdstuff.activity.base.BaseActivity;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.types.LineAlias;
import me.miran.mhdstuff.raptor.Path;
import me.miran.mhdstuff.raptor.PathNode;
import me.miran.mhdstuff.raptor.Raptor;

public class ConnectionsResultScreen extends BaseActivity {
    public ConnectionsResultScreen() {
        super("Spojení", R.layout.activity_connection_result);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();


            List<Path> paths = Raptor.getDepartures((short) 847, (short) 2840);

            runOnUiThread(() -> create(storage, paths));
        }).start();
    }


    private void create(IdStorage storage, List<Path> paths) {
//        LinearLayout ll = findViewById(R.id.list);

        LinearLayout fin = findViewById(R.id.list);
        LinearLayout ll = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.empty_linear_layout,fin , false);

        var orig = ll;
        ll = ll.findViewById(R.id.list);

        int ind = 0;

        List<PathNode> nodes = paths.get(0).nodes();
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



        fin.addView(orig, 0);
    }

}
