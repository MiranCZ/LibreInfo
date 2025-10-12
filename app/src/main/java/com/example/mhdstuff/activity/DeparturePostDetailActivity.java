package com.example.mhdstuff.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.activity.data.DelaysDataHolder;
import com.example.mhdstuff.activity.data.PostDataHolder;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Post;
import com.example.mhdstuff.parsing.types.departure.Departure;
import com.example.mhdstuff.util.OfflineDepartures;
import com.google.gson.JsonObject;

import java.util.List;

public class DeparturePostDetailActivity extends BaseActivity {


    private final Post post;
    public DeparturePostDetailActivity() {
        super(PostDataHolder.getPost().name());
        this.post = PostDataHolder.getPost();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_deparute_post_detail);

        Context context = this;
        JsonObject delays = DelaysDataHolder.getDelays();

        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();

            List<Departure> departureList = OfflineDepartures.getOffline(storage, post.stopID(), 100, delays);

            Departure departure = departureList.stream().filter(dep -> dep.postID() == post.postID()).findFirst().orElse(null);

            runOnUiThread(() -> {
                FrameLayout layout = findViewById(R.id.departure_content);
                View departureView = departure.createScrollableDepartureView(this, layout, context);
                departureView.setFocusable(false);
                departureView.setClickable(false);

                layout.addView(departureView, 0);
            });
        }).start();



    }
}
