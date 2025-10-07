package com.example.mhdstuff.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.NewsItemAdapter;
import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.parsing.types.News;
import com.example.mhdstuff.util.CacheHelper;

import java.util.List;

public class NewsActivity extends BaseActivity {

    private List<News> items;
    private RecyclerView recyclerView;
    private NewsItemAdapter adapter;

    public NewsActivity() {
        super("Novinky");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        recyclerView = findViewById(R.id.news_view_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Context context = this;
        new Thread(() -> {
            items = News.parseNewsList(CacheHelper.getNews(context));
            System.out.println("Loaded "+items);

            adapter = new NewsItemAdapter(items, context);
            runOnUiThread(() -> recyclerView.setAdapter(adapter));
//            recyclerView.setAdapter(adapter);
        }).start();

    }


}
