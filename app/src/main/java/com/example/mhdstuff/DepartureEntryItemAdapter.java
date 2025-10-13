package com.example.mhdstuff;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.activity.listview.AbstractItemAdapter;
import com.example.mhdstuff.parsing.types.Time;
import com.example.mhdstuff.parsing.types.departure.DepartureEntry;

import java.time.LocalTime;
import java.util.List;

public class DepartureEntryItemAdapter extends AbstractItemAdapter<DepartureEntry, DepartureEntryItemAdapter.DepartureEntryHolder> {


    private final BaseActivity parent;

    public DepartureEntryItemAdapter(BaseActivity parent, List<DepartureEntry> items) {
        super(items, R.layout.departure_entry_layout);
        this.parent = parent;
    }

    @Override
    protected void bindValues(DepartureEntryHolder holder, DepartureEntry item) {
        item.populateDepartureViewEntry(parent, parent, holder.itemView);

        if (item.timeMark().time().isBefore(Time.now()) && !item.timeMark().isLeaving()) {
            TextView text = holder.itemView.findViewById(R.id.departure_heading);

            SpannableString str = new SpannableString(text.getText());
            str.setSpan(new ForegroundColorSpan(ContextCompat.getColor(parent, R.color.secondary_color_dark_tone)), 0, str.length(), 0);

            text.setText(str);

            text = holder.itemView.findViewById(R.id.departure_arrival);

            str = new SpannableString(text.getText());
            str.setSpan(new ForegroundColorSpan(ContextCompat.getColor(parent, R.color.secondary_color_dark_tone)), 0, str.length(), 0);

            text.setText(str);
        }
    }

    @Override
    protected DepartureEntryHolder createHolder(View view) {
        return new DepartureEntryHolder(view);
    }

    protected static class DepartureEntryHolder extends ItemViewHolder {

        public DepartureEntryHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
