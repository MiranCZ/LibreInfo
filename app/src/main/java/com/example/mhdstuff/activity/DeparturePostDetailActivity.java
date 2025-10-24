package com.example.mhdstuff.activity;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.DepartureEntryItemAdapter;
import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.data.Arg;
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


    private final Arg<Post> post;
    private final JsonObject delays;

    public DeparturePostDetailActivity() {
        super("", R.layout.activity_deparute_post_detail, R.id.departure_content);
        this.post = popArg("post", null);

        this.delays = DelaysDataHolder.getDelays();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setName(post.get().name());
    }

    @Override
    protected RecyclerView.Adapter<?> getAdapter(Context context, IdStorage storage) {
        Post post = this.post.get();
        JsonObject stopDelays = null;
        try {
            stopDelays = RequestHelper.getStopDelays(post.stopID());
        } catch (RequestException e) {
            runOnUiThread(() -> e.showError(this, AppException.NotificationType.SNACK_BAR));
        }

        List<Departure> departureList = OfflineDepartures.getOffline(storage, post.stopID(), -1, Time.ZERO, delays);

        Departure departure = departureList.stream().filter(dep -> dep.postID() == post.postID()).findFirst().orElse(null);

        TextView title = findViewById(R.id.departure_title);
        title.setText(departure.name());

        var adapter = new DepartureEntryItemAdapter(this, departure.entries(),storage.apiStorage(), stopDelays);

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
