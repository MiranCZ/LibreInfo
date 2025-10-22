package com.example.mhdstuff;

import android.content.Intent;
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

import com.example.mhdstuff.activity.DiversionInfoActivity;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.activity.listview.AbstractItemAdapter;
import com.example.mhdstuff.activity.listview.AbstractListViewActivity;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Diversion;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.DateTime;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;
import java.util.function.Consumer;

public class DiversionsItemAdapter extends AbstractItemAdapter<Diversion, DiversionsItemAdapter.DiversionViewHolder> {


    private final AbstractListViewActivity activity;

    public DiversionsItemAdapter(List<Diversion> items, AbstractListViewActivity activity) {
        super(items, R.layout.diversion_item_layout);
        this.activity = activity;
    }

    @Override
    protected void bindValues(DiversionViewHolder holder, Diversion item) {
        IdStorage.getInstanceOnUIThread(
                (storage) -> createElement(holder, item, storage), activity
        );
    }

    private void createElement(DiversionViewHolder holder, Diversion item, IdStorage storage) {
        holder.title.setText(item.title());

        // FIXME hardcoded strings
        holder.from.setText(createSpannable("od: ", item.from()));
        holder.to.setText(createSpannable("do: ", item.to()));

        holder.view.setOnClickListener(view -> {
            activity.startActivity(
                    DiversionInfoActivity.class,
                    intent -> BaseActivity.putArg(intent, "diversion", item)
            );
        });

        FlexboxLayout lines = holder.lines;

        lines.removeAllViews();
        for (LineAlias alias : item.lines()) {
            lines.addView(alias.createLineIconView(lines, activity));
        }
    }

    private SpannableString createSpannable(String info, DateTime time) {
        if (time == null || time == DateTime.NONE) {
            return new SpannableString("");
        }

        SpannableString spannable = new SpannableString(info+time.toString());
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.secondary_color_tone)), 0, info.length(), 0);

        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.secondary_color_light_tone)), info.length(), spannable.length(), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface font = ResourcesCompat.getFont(activity, R.font.roboto_bold);
            if (font != null) {
                spannable.setSpan(new TypefaceSpan(font), info.length(), spannable.length(), 0);
            }
        }

        return spannable;
    }


    @Override
    protected DiversionViewHolder createHolder(View view) {
        return new DiversionViewHolder(view);
    }

    protected static class DiversionViewHolder extends ItemViewHolder {

        View view;
        TextView title;
        TextView from;
        TextView to;
        FlexboxLayout lines;

        public DiversionViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.diversions_container);
            title = itemView.findViewById(R.id.diversion_title);
            from = itemView.findViewById(R.id.diversion_from);
            to = itemView.findViewById(R.id.diversion_to);
            lines = itemView.findViewById(R.id.diversion_line_list);
        }
    }
}