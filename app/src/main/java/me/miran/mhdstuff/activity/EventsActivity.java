package me.miran.mhdstuff.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import me.miran.mhdstuff.EventsItemAdapter;
import me.miran.mhdstuff.R;

import me.miran.mhdstuff.activity.listview.AbstractListViewActivity;
import me.miran.mhdstuff.exception.AppException;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.types.Event;
import me.miran.mhdstuff.util.request.RequestHelper;

import java.util.ArrayList;
import java.util.List;

public class EventsActivity extends AbstractListViewActivity {

    public EventsActivity() {
        super(R.string.events, R.layout.activity_diversions, R.id.diversions_view_items);
    }

    @Override
    protected RecyclerView.Adapter<?> getAdapter(Context context, IdStorage storage) {
        List<Event> events;

        try {
            events = Event.parseEvents(RequestHelper.getEvents(context), storage.lineStorage());
        } catch (AppException e) {
            e.printStackTrace();
            e.showError(this);
            events = new ArrayList<>();
        }

        if (events.isEmpty()) {
            runOnUiThread(() -> {
                ViewGroup content = findViewById(R.id.diversion_content);
                LayoutInflater.from(this).inflate(R.layout.nothing_here_layout, content);
            });
        }

        return new EventsItemAdapter(events, this);
    }

}
