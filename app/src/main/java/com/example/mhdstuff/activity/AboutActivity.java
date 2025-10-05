package com.example.mhdstuff.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.mhdstuff.R;

public class AboutActivity extends BaseActivity {


    public AboutActivity() {
        super("O aplikaci");
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
    }
}
