package me.miran.libreinfo.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import me.miran.libreinfo.NewsItemAdapter;
import me.miran.libreinfo.R;
import me.miran.libreinfo.activity.base.BaseActivity;
import me.miran.libreinfo.parsing.types.News;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends BaseActivity {

    private List<News> items;
    private RecyclerView recyclerView;
    private NewsItemAdapter adapter;

    public NewsActivity() {
        super(R.string.news, R.layout.activity_news);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recyclerView = findViewById(R.id.news_view_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Context context = this;
        new Thread(() -> {
//            items = News.parseNewsList(CacheHelper.getNews(context));
            items = new ArrayList<>();
            System.out.println("Loaded "+items);

            adapter = new NewsItemAdapter(items, context);
            runOnUiThread(() -> recyclerView.setAdapter(adapter));
//            recyclerView.setAdapter(adapter);
        }).start();

    }


}
