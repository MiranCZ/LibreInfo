package com.example.mhdstuff.activity;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.EventsItemAdapter;
import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.listview.AbstractListViewActivity;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Event;
import com.example.mhdstuff.util.request.RequestHelper;

public class EventsActivity extends AbstractListViewActivity {

    public EventsActivity() {
        super(R.string.events, R.layout.activity_diversions, R.id.diversions_view_items);
    }

    @Override
    protected RecyclerView.Adapter<?> getAdapter(Context context, IdStorage storage) {
        return new EventsItemAdapter(Event.parseEvents(RequestHelper.getEvents(), storage.lineStorage()), this);
    }

}
