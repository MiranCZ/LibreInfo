package com.example.mhdstuff.activity;

import android.graphics.PorterDuff;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.mhdstuff.R;
import com.google.android.material.appbar.MaterialToolbar;

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




        if (getParentActivityIntent() != null) {
            toolbar.setNavigationIcon(R.drawable.chevron_left);
            toolbar.getNavigationIcon().setColorFilter(ContextCompat.getColor(this, R.color.light_blue), PorterDuff.Mode.SRC_ATOP);

            toolbar.setNavigationOnClickListener(v -> onNavigateUp());
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    protected void addButtonIcon(int iconResId, View.OnClickListener listener) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        AppCompatImageButton button = new AppCompatImageButton(this);

        button.setImageResource(iconResId);

        int tintColor = ContextCompat.getColor(this, R.color.light_blue);
        DrawableCompat.setTint(button.getDrawable().mutate(), tintColor);

        int sizePx = dpToPx(50);
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(sizePx, sizePx);
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        params.setMarginEnd(dpToPx(4));
        button.setLayoutParams(params);

        button.setBackgroundResource(R.drawable.icon_ripple_rounded);

        button.setScaleType(AppCompatImageButton.ScaleType.FIT_CENTER);

        button.setOnClickListener(listener);

        toolbar.addView(button);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

}
