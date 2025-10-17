package com.example.mhdstuff.activity.base;

import android.app.Activity;
import android.content.Intent;
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
import com.example.mhdstuff.util.Text;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.function.Consumer;

public abstract class BaseActivity extends AppCompatActivity {

    private final Text name;
    private final Integer layoutId;

    public BaseActivity(int nameId) {
        this(nameId, null);
    }

    public BaseActivity(int nameId, Integer layoutId) {
        this.name = Text.translatable(nameId);
        this.layoutId = layoutId;
    }

    public BaseActivity(String name) {
        this(name, null);
    }

    public BaseActivity(String name, Integer layoutId) {
        this.name = Text.literal(name);
        this.layoutId = layoutId;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (layoutId != null) {
            setContentView(layoutId);
        }
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
        toolbar.setTitle(name.getName(this));
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

    protected View addButtonIcon(int iconResId, View.OnClickListener listener) {
        return addButtonIcon(iconResId, listener, true);
    }

    protected View addButtonIcon(int iconResId, View.OnClickListener listener, boolean addTint) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        AppCompatImageButton button = new AppCompatImageButton(this);

        button.setImageResource(iconResId);

        if (addTint) {
            int tintColor = ContextCompat.getColor(this, R.color.light_blue);
            DrawableCompat.setTint(button.getDrawable().mutate(), tintColor);
        }

        int sizePx = dpToPx(50);
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(sizePx, sizePx);
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        params.setMarginEnd(dpToPx(4));
        button.setLayoutParams(params);

        button.setBackgroundResource(R.drawable.icon_ripple_rounded);

        button.setScaleType(AppCompatImageButton.ScaleType.FIT_CENTER);

        button.setOnClickListener(listener);

        toolbar.addView(button);

        return button;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void startActivity(Class<? extends Activity> clazz) {
        startActivity(clazz, (ignored) -> {});
    }

    public void startActivity(Class<? extends Activity> clazz, Consumer<Intent> intentSetup) {
        Intent intent = new Intent(this, clazz);

        intentSetup.accept(intent);
        startActivity(intent);
        overridePendingTransition(R.anim.fast_scale_up, R.anim.fast_fade_out);

    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(R.anim.fast_fade_in, R.anim.fast_scale_down);
    }

    @Override
    public boolean onNavigateUp() {
        boolean result = super.onNavigateUp();
        overridePendingTransition(R.anim.fast_fade_in, R.anim.fast_scale_down);
        return result;
    }


}
