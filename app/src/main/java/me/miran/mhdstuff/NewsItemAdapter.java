package me.miran.mhdstuff;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import me.miran.mhdstuff.activity.listview.AbstractItemAdapter;
import me.miran.mhdstuff.parsing.types.News;

import java.util.List;

public class NewsItemAdapter extends AbstractItemAdapter<News, NewsItemAdapter.NewsViewHolder> {

    private final Context context;


    public NewsItemAdapter(List<News> items, Context context) {
        super(items, R.layout.news_item_layout);
        this.context = context;
    }

    @Override
    protected void bindValues(NewsViewHolder holder, News currentItem) {
        holder.title.setText(currentItem.title());
        holder.date.setText(currentItem.publicFrom().toString());
        holder.content.setText(currentItem.text());
        holder.container.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentItem.link()));
            System.out.println("CALLED CLICK");
            context.startActivity(intent);
        });
    }

    @Override
    protected NewsViewHolder createHolder(View view) {
        return new NewsViewHolder(view);
    }

    protected static class NewsViewHolder extends ItemViewHolder {

        TextView title;
        TextView date;
        TextView content;

        LinearLayout container;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.news_title);
            date = itemView.findViewById(R.id.news_date);
            content = itemView.findViewById(R.id.news_content);
            container = itemView.findViewById(R.id.news_container);
        }
    }
}