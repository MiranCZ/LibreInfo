package me.miran.mhdstuff.activity.devtest;

import android.os.Bundle;

import me.miran.mhdstuff.R;
import me.miran.mhdstuff.activity.base.BaseActivity;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.storage.LineStorage;
import me.miran.mhdstuff.parsing.types.LineAlias;
import com.google.android.flexbox.FlexboxLayout;

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

        // TODO get only line storage?
        IdStorage.getInstanceOnUIThread((storage) -> {
            LineStorage lineStorage = storage.lineStorage();

            items = lineStorage.getAllAliases();
            items.sort(Comparator.comparing(l -> l.getSortKey(lineStorage)));

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
