package com.example.mhdstuff.activity;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.DepartureEntryItemAdapter;
import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.data.DelaysDataHolder;
import com.example.mhdstuff.activity.listview.AbstractListViewActivity;
import com.example.mhdstuff.exception.AppException;
import com.example.mhdstuff.exception.RequestException;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Post;
import com.example.mhdstuff.parsing.types.Time;
import com.example.mhdstuff.parsing.types.departure.Departure;
import com.example.mhdstuff.util.OfflineDepartures;
import com.example.mhdstuff.util.request.RequestHelper;
import com.google.gson.JsonObject;

import java.util.List;

public class DeparturePostDetailActivity extends AbstractListViewActivity {


    private final JsonObject delays;
    private Post post;

    public DeparturePostDetailActivity() {
        super("", R.layout.activity_deparute_post_detail, R.id.departure_content);

        this.delays = DelaysDataHolder.getDelays();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        post = getIntent().getParcelableExtra("post");
        setName(post.name());
    }

    @Override
    protected RecyclerView.Adapter<?> getAdapter(Context context, IdStorage storage) {
        JsonObject stopDelays = new JsonObject();
        try {
            stopDelays = RequestHelper.getStopDelays(post.stopID());
        } catch (RequestException e) {
            runOnUiThread(() -> e.showError(this, AppException.NotificationType.SNACK_BAR));
        }

        List<Departure> departureList = OfflineDepartures.getOfflineForPost(storage, post.stopID(),post.postID(), -1, Time.ZERO, delays);

        Departure departure = departureList.stream().filter(dep -> dep.postID() == post.postID()).findFirst().orElse(null);

        TextView title = findViewById(R.id.departure_title);
        title.setText(departure.name());

        var adapter = new DepartureEntryItemAdapter(this, departure.entries(),storage.apiStorage(), stopDelays);

        Time now = Time.now();

        int firstPos = -1;
        for (int i = 0; i < departure.entries().size(); i++) {
            var entry = departure.entries().get(i);
            if (!entry.timeMark().time().isBefore(now)) {
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
