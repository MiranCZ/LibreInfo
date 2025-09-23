package com.example.mhdstuff.activity.listview;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.R;
import com.example.mhdstuff.parsing.storage.IdStorage;

public abstract class AbstractListViewActivity extends AppCompatActivity {

    private final String name;
    private final int layoutId;
    private final int recycleViewId;

    public AbstractListViewActivity(String name, int layoutId, int recycleViewId) {
        this.name = name;
        this.layoutId = layoutId;
        this.recycleViewId = recycleViewId;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layoutId);
        RecyclerView recyclerView = findViewById(recycleViewId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Context context = this;
        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();
            RecyclerView.Adapter adapter = getAdapter(context, storage);
            runOnUiThread(() -> {
                var spinner =  findViewById(R.id.loading_spinner);
                if (spinner != null) {
                    spinner.setVisibility(View.GONE);
                }
                recyclerView.setAdapter(adapter);
            });
        }).start();


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(name);
        }
    }

    protected abstract RecyclerView.Adapter<?> getAdapter(Context context, IdStorage storage);

}
