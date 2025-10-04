package com.example.mhdstuff.activity.devtest;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.DiversionsItemAdapter;
import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.BaseActivity;
import com.example.mhdstuff.activity.MainActivity;
import com.example.mhdstuff.activity.listview.AbstractItemAdapter;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.TransportLine;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.Comparator;
import java.util.List;

public class LineListActivity extends BaseActivity {

    private List<LineAlias> items;

    public LineListActivity() {
        super("Line test");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_test);

        IdStorage.getInstanceOnUIThread((storage) -> {
            items = storage.lineStorage().getAllAliases();
            items.sort(Comparator.comparing(LineAlias::id));

            runOnUiThread(() -> {
                FlexboxLayout lines = findViewById(R.id.line_items);

                System.out.println("ADDING "+items.size());
                for (LineAlias line : items) {
                    lines.addView(line.createLineIconView(lines, this));
                }
            });
        }, this);

    }



}
