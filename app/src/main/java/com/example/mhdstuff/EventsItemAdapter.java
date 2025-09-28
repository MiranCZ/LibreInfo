package com.example.mhdstuff;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.mhdstuff.activity.DiversionInfoActivity;
import com.example.mhdstuff.activity.data.DiversionDataHolder;
import com.example.mhdstuff.activity.listview.AbstractItemAdapter;
import com.example.mhdstuff.activity.listview.AbstractListViewActivity;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.DateTime;
import com.example.mhdstuff.parsing.types.Diversion;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.TransportLine;
import com.example.mhdstuff.util.Pair;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

public class EventsItemAdapter extends AbstractItemAdapter<Diversion, EventsItemAdapter.EventItemHodler> {


    private final AbstractListViewActivity activity;

    public EventsItemAdapter(List<Diversion> items, AbstractListViewActivity activity) {
        super(items, R.layout.event_item_layout);
        this.activity = activity;
    }

    @Override
    protected void bindValues(EventItemHodler holder, Diversion item) {
        IdStorage.getInstanceOnUIThread(
                (storage) -> createElement(holder, item, storage), activity
        );
    }

    private void createElement(EventItemHodler holder, Diversion item, IdStorage storage) {
        holder.title.setText(item.title());

        // FIXME hardcoded strings
        holder.times.setText(createSpannable(item.from(), item.to()));

        FlexboxLayout lines = holder.lines;

        lines.removeAllViews();
        for (TransportLine line : item.lines()) {
            LineAlias alias = storage.lineStorage().getAlias(line.id());

            lines.addView(alias.createLineIconView(lines, activity));
        }

        holder.content.setText(Html.fromHtml(item.publicText(), 0));
    }

    private SpannableString createSpannable(DateTime from, DateTime to) {
        Pair<Integer, String> formatted = DateTime.toShortenedInformedString(from, to);
        int first = formatted.left();

        SpannableString spannable = new SpannableString(formatted.right());
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.secondary_color_light_tone)), 0,first , 0);

        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.secondary_color_tone)), first, first+3, 0);

        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.secondary_color_light_tone)), first+3,spannable.length() , 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface font = ResourcesCompat.getFont(activity, R.font.roboto_bold);
            if (font != null) {
                spannable.setSpan(new TypefaceSpan(font), 0, first, 0);
                spannable.setSpan(new TypefaceSpan(font), first+3, spannable.length(), 0);
            }
        }

        return spannable;
    }


    @Override
    protected EventItemHodler createHolder(View view) {
        return new EventItemHodler(view);
    }

    protected static class EventItemHodler extends ItemViewHolder {

        View view;
        TextView title;
        TextView times;
        FlexboxLayout lines;
        TextView content;

        public EventItemHodler(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.event_container);
            title = itemView.findViewById(R.id.event_title);
            times = itemView.findViewById(R.id.event_times);
            lines = itemView.findViewById(R.id.event_line_list);
            content = itemView.findViewById(R.id.event_content);
        }
    }
}