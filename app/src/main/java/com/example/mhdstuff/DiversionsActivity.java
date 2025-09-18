package com.example.mhdstuff;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.activity.MainActivity;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.List;

public class DiversionsActivity extends AppCompatActivity {

    private List<LineAlias> items;
    private RecyclerView recyclerView;
    private DiversionsItemAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diversions);
        recyclerView = findViewById(R.id.diversions_view_items);
        recyclerView.setLayoutManager(new FlexboxLayoutManager(this));

        Context context = this;
        new Thread(() -> {
//            items = Diversion.parseDiversions(RequestHelper.getDiversions(), MainActivity.storage.lineStorage());
            items = MainActivity.storage.lineStorage().getAllAliases();
            System.out.println("Loaded "+items.size());

            adapter = new DiversionsItemAdapter(items, context);
            runOnUiThread(() -> recyclerView.setAdapter(adapter));
//            recyclerView.setAdapter(adapter);
        }).start();

        // For the back button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Novinky");
        }

    }


}
