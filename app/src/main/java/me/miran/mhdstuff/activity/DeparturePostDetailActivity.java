package me.miran.mhdstuff.activity;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import me.miran.mhdstuff.DepartureEntryItemAdapter;
import me.miran.mhdstuff.R;
import me.miran.mhdstuff.activity.data.DelaysDataHolder;
import me.miran.mhdstuff.activity.listview.AbstractListViewActivity;
import me.miran.mhdstuff.exception.AppException;
import me.miran.mhdstuff.exception.RequestException;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.types.Post;
import me.miran.mhdstuff.parsing.types.Time;
import me.miran.mhdstuff.parsing.types.departure.Departure;
import me.miran.mhdstuff.util.OfflineDepartures;
import me.miran.mhdstuff.util.request.RequestHelper;
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
            stopDelays = RequestHelper.getStopDelays(context, post.stop().id);
        } catch (RequestException e) {
            runOnUiThread(() -> e.showError(this, AppException.NotificationType.SNACK_BAR));
        }

        List<Departure> departureList = OfflineDepartures.getOfflineForPost(storage, post.stop().id.internal(),post.postID(), -1, Time.ZERO, delays);

        Departure departure = departureList.stream().filter(dep -> dep.postID() == post.postID()).findFirst().orElse(null);

        runOnUiThread(() -> {
            TextView title = findViewById(R.id.departure_title);
            title.setText(departure.name());
        });
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
