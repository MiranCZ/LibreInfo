package com.example.mhdstuff;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.parsing.types.Diversion;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.News;

import java.util.List;

public class DiversionsItemAdapter extends RecyclerView.Adapter<DiversionsItemAdapter.DiversionViewHolder> {

    private List<LineAlias> items;
    private final Context context;


    public DiversionsItemAdapter(List<LineAlias> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public DiversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.line_icon_layout, parent, false);
        return new DiversionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiversionViewHolder holder, int position) {
        LineAlias currentItem = items.get(position);

        holder.title.setText(currentItem.lineDisplayName());
        holder.title.setTextColor(currentItem.textColor());
        holder.back.setColor(currentItem.backgroundColor());

        holder.view.post(new Runnable() {
            @Override
            public void run() {
                holder.view.setMinimumWidth(holder.view.getHeight());
            }
        });
//        System.out.println("creating "+currentItem);
//        holder.title.setText(currentItem.title());
//        holder.date.setText(currentItem.publicFrom().toString());
//        holder.content.setText(currentItem.text());
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class DiversionViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        View view;
        GradientDrawable back;
        public DiversionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.line_name);

            var v = itemView.findViewById(R.id.diversions_container);

            this.view = v;
            back = (GradientDrawable) v.getBackground();
            ((GradientDrawable)v.getBackground()).setColor(Color.parseColor("#00FF00"));
            ((TextView)v.findViewById(R.id.line_name)).setTextColor(Color.parseColor("#FFFFFF"));

//            itemView.findViewById(R.id.diversions_container).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//                @Override
//                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
//                    int height = view.getHeight();
//                    System.out.println("layout IS CHANGED "+height);
//                    view.setMinimumWidth(1000);
//                    view.notify();
//                }
//            });
//            title = itemView.findViewById(R.id.news_title);
//            date = itemView.findViewById(R.id.news_date);
//            content = itemView.findViewById(R.id.news_content);
//            container = itemView.findViewById(R.id.news_container);
        }
    }
}