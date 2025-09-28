package com.example.mhdstuff.activity;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.EventsItemAdapter;
import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.listview.AbstractListViewActivity;
import com.example.mhdstuff.parsing.TrafficChangesManager;
import com.example.mhdstuff.parsing.storage.IdStorage;

public class EventsActivity extends AbstractListViewActivity {

    public EventsActivity() {
        super("Mimořádné události", R.layout.activity_diversions, R.id.diversions_view_items);
    }

    @Override
    protected RecyclerView.Adapter<?> getAdapter(Context context, IdStorage storage) {
        return new EventsItemAdapter(
                TrafficChangesManager.parse(
                        ".views-infinite-scroll-content-wrapper",
                        storage.lineStorage()
                ), this
        );
    }

}
