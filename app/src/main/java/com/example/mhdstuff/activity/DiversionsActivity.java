package com.example.mhdstuff.activity;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.DiversionsItemAdapter;
import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.listview.AbstractListViewActivity;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Diversion;
import com.example.mhdstuff.util.RequestHelper;

import java.util.List;

public class DiversionsActivity extends AbstractListViewActivity {

    public DiversionsActivity() {
        super("Změny v dopravě", R.layout.activity_diversions, R.id.diversions_view_items);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(Context context, IdStorage storage) {
        List<Diversion> items = Diversion.parseDiversions(RequestHelper.getDiversions(), storage.lineStorage());

        return new DiversionsItemAdapter(items, this);
    }


}
