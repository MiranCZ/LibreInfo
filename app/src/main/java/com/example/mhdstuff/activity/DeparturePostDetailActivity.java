package com.example.mhdstuff.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.DepartureEntryItemAdapter;
import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.activity.data.DelaysDataHolder;
import com.example.mhdstuff.activity.data.PostDataHolder;
import com.example.mhdstuff.activity.listview.AbstractListViewActivity;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Post;
import com.example.mhdstuff.parsing.types.Time;
import com.example.mhdstuff.parsing.types.departure.Departure;
import com.example.mhdstuff.util.OfflineDepartures;
import com.google.gson.JsonObject;

import java.time.LocalTime;
import java.util.List;

public class DeparturePostDetailActivity extends AbstractListViewActivity {


    private final Post post;
    private final JsonObject delays
            ;

    public DeparturePostDetailActivity() {
        // FIXME departure scrollable layout??
        super(PostDataHolder.getPost().name(), R.layout.activity_deparute_post_detail, R.id.departure_content);
        this.post = PostDataHolder.getPost();
        this.delays = DelaysDataHolder.getDelays();
    }

    @Override
    protected RecyclerView.Adapter<?> getAdapter(Context context, IdStorage storage) {

        List<Departure> departureList = OfflineDepartures.getOffline(storage, post.stopID(), -1, Time.ZERO, delays);

        Departure departure = departureList.stream().filter(dep -> dep.postID() == post.postID()).findFirst().orElse(null);

        TextView title = findViewById(R.id.departure_title);
        title.setText(departure.name());


        var adapter = new DepartureEntryItemAdapter(this, departure.entries());


        int firstPos = -1;
        for (int i = 0; i < departure.entries().size(); i++) {
            var entry = departure.entries().get(i);
            if (entry.timeMark().time().isAfter(Time.now())) {
                firstPos = i;
                break;
            }
        }
        int finalFirstPos = firstPos;
        runOnUiThread(() -> {
            RecyclerView recyclerView = findViewById(recycleViewId);
            recyclerView.scrollToPosition(finalFirstPos);
        });

        return adapter;
    }
}
