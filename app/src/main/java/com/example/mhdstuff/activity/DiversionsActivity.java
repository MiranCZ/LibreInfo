package com.example.mhdstuff.activity;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.DiversionsItemAdapter;
import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.listview.AbstractListViewActivity;
import com.example.mhdstuff.exception.AppException;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Diversion;
import com.example.mhdstuff.util.request.RequestHelper;

import java.util.ArrayList;
import java.util.List;

public class DiversionsActivity extends AbstractListViewActivity {

    public DiversionsActivity() {
        super(R.string.diversions, R.layout.activity_diversions, R.id.diversions_view_items);
    }

    @Override
    protected RecyclerView.Adapter<?> getAdapter(Context context, IdStorage storage) {
        List<Diversion> diversions;

        try {
            diversions = Diversion.parseDiversions(RequestHelper.getDiversions(),storage.lineStorage());
        } catch (AppException e) {
            e.showErrPopup(this);
            diversions = new ArrayList<>();
        }

        return new DiversionsItemAdapter(diversions, this);
    }


}
