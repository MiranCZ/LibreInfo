package com.example.mhdstuff.activity;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.mhdstuff.R;

public abstract class BaseActivity extends AppCompatActivity {

    private final String name;

    public BaseActivity(String name) {
        this.name = name;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        // Inflate the base layout which contains the toolbar and a content_frame.
        ViewGroup base = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_base, null);
        // Inflate the child layout into the content_frame
        View child = LayoutInflater.from(this).inflate(layoutResID, base.findViewById(R.id.content_frame), false);
        base.findViewById(R.id.content_frame).setVisibility(View.VISIBLE);
        ((ViewGroup) base.findViewById(R.id.content_frame)).addView(child);
        super.setContentView(base);

        // Setup toolbar as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayUseLogoEnabled(true);


        if (getParentActivityIntent() != null) {
            toolbar.setNavigationIcon(R.drawable.chevron_left);
            toolbar.getNavigationIcon().setColorFilter(ContextCompat.getColor(this, R.color.light_blue), PorterDuff.Mode.SRC_ATOP);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNavigateUp();
                }
            });
        }

        // optional: enable up button by default
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    /**
     * Helper child activities can override to provide menu resource id.
     * Return 0 if none.
     */
    protected int getMenuResourceId() {
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        int menuId = getMenuResourceId();
        if (menuId != 0) getMenuInflater().inflate(menuId, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
