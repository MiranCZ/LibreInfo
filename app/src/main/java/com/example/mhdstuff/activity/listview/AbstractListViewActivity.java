package com.example.mhdstuff.activity.listview;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AbstractListViewActivity<T> extends AppCompatActivity {

    private final String name;

    public AbstractListViewActivity(String name) {
        this.name = name;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(name);
        }
    }

    protected abstract
}
