package com.example.mhdstuff;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.activity.MainActivity;
import com.example.mhdstuff.activity.listview.AbstractItemAdapter;
import com.example.mhdstuff.parsing.types.Diversion;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.News;
import com.example.mhdstuff.parsing.types.Time;
import com.example.mhdstuff.parsing.types.TransportLine;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

public class DiversionsItemAdapter extends AbstractItemAdapter<Diversion, DiversionsItemAdapter.DiversionViewHolder> {


    private final Context context;

    public DiversionsItemAdapter(List<Diversion> items, Context context) {
        super(items, R.layout.diversion_item_layout);
        this.context = context;
    }

    @Override
    protected void bindValues(DiversionViewHolder holder, Diversion item) {
        holder.title.setText(item.title());

        // FIXME hardcoded strings
        holder.from.setText(createSpannable("od: ", item.from()));
        holder.to.setText(createSpannable("do: ", item.to()));

        FlexboxLayout lines = holder.lines;

        lines.removeAllViews();
        for (TransportLine line : item.lines()) {
            LineAlias alias = MainActivity.storage.lineStorage().getAlias(line.id());

            lines.addView(createLineIconView(alias, lines));
        }
    }

    private SpannableString createSpannable(String info, Time time) {
        SpannableString spannable = new SpannableString(info+time.toString());
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.secondary_color_tone)), 0, info.length(), 0);

        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.secondary_color_light_tone)), info.length(), spannable.length(), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface font = ResourcesCompat.getFont(context, R.font.roboto_bold);
            if (font != null) {
                spannable.setSpan(new TypefaceSpan(font), info.length(), spannable.length(), 0);
            }
        }

        return spannable;
    }

    private View createLineIconView(LineAlias lineAlias, FlexboxLayout layout) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.line_icon_layout, layout , false);
        TextView title = itemView.findViewById(R.id.line_name);
        title.setText(lineAlias.lineDisplayName());
        title.setTextColor(lineAlias.textColor());

        View view = itemView.findViewById(R.id.icon_container);
        GradientDrawable back = (GradientDrawable) view.getBackground();
        back.setColor(lineAlias.backgroundColor());

        view.post(() -> view.setMinimumWidth(view.getHeight()));

        return itemView;
    }

    @Override
    protected DiversionViewHolder createHolder(View view) {
        return new DiversionViewHolder(view);
    }

    protected static class DiversionViewHolder extends ItemViewHolder {

        TextView title;

        TextView from;
        TextView to;
        FlexboxLayout lines;

        public DiversionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.diversion_title);
            from = itemView.findViewById(R.id.diversion_from);
            to = itemView.findViewById(R.id.diversion_to);
            lines = itemView.findViewById(R.id.diversion_line_list);
        }
    }
}