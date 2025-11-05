package me.miran.mhdstuff;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import me.miran.mhdstuff.activity.listview.AbstractItemAdapter;
import me.miran.mhdstuff.activity.listview.AbstractListViewActivity;
import me.miran.mhdstuff.parsing.types.DateTime;
import me.miran.mhdstuff.parsing.types.Event;
import me.miran.mhdstuff.parsing.types.LineAlias;
import me.miran.mhdstuff.util.HtmlHelper;
import me.miran.mhdstuff.util.Pair;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

public class EventsItemAdapter extends AbstractItemAdapter<Event, EventsItemAdapter.EventItemHodler> {


    private final AbstractListViewActivity activity;

    public EventsItemAdapter(List<Event> items, AbstractListViewActivity activity) {
        super(items, R.layout.event_item_layout);
        this.activity = activity;
    }

    @Override
    protected void bindValues(EventItemHodler holder, Event item) {
        createElement(holder, item);
    }

    private void createElement(EventItemHodler holder, Event item) {
        holder.title.setText(item.title());

        holder.times.setText(createSpannable(item.from(), item.to()));

        final String text = activity.getString(R.string.vehicle_delay) + " ";
        SpannableString span = new SpannableString(text+item.delay()+" min");
        span.setSpan(new ForegroundColorSpan(Color.RED), text.length(), span.length(), 0);

        holder.delay.setText(span);

        FlexboxLayout lines = holder.lines;

        lines.removeAllViews();
            for (LineAlias alias : item.lines()) {
            lines.addView(alias.createLineIconView(lines, activity));
        }

        if (item.text().isBlank()) {
            holder.content.setVisibility(View.GONE);
        } else {
            holder.content.setVisibility(View.VISIBLE);

            holder.content.setText(HtmlHelper.parseHtml(item.text()));
        }
    }

    private SpannableString createSpannable(DateTime from, DateTime to) {
        Pair<Integer, String> formatted = DateTime.toShortenedInformedString(from, to);
        int first = formatted.left();

        // only one element
        if (first == -1) {
            SpannableString spannable = new SpannableString(formatted.right());
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.secondary_color_light_tone)), 0,spannable.length() , 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Typeface font = ResourcesCompat.getFont(activity, R.font.roboto_bold);
                if (font != null) {
                    spannable.setSpan(new TypefaceSpan(font), 0, spannable.length(), 0);
                }
            }

            return spannable;
        }

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
        TextView delay;
        FlexboxLayout lines;
        TextView content;

        public EventItemHodler(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.event_container);
            title = itemView.findViewById(R.id.event_title);
            times = itemView.findViewById(R.id.event_times);
            delay = itemView.findViewById(R.id.event_delay);
            lines = itemView.findViewById(R.id.event_line_list);
            content = itemView.findViewById(R.id.event_content);
        }
    }
}