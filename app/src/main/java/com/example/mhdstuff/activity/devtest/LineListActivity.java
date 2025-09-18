package com.example.mhdstuff.activity.devtest;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.DiversionsItemAdapter;
import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.MainActivity;
import com.example.mhdstuff.activity.listview.AbstractItemAdapter;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.List;

public class LineListActivity extends AppCompatActivity {

    private List<LineAlias> items;
    private RecyclerView recyclerView;
    private ItemAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diversions);
        recyclerView = findViewById(R.id.diversions_view_items);
        recyclerView.setLayoutManager(new FlexboxLayoutManager(this));

        Context context = this;
        new Thread(() -> {
            items = MainActivity.storage.lineStorage().getAllAliases();

            adapter = new ItemAdapter(items);
            runOnUiThread(() -> recyclerView.setAdapter(adapter));
        }).start();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Line List Test");
        }

    }


    private static class ItemAdapter extends AbstractItemAdapter<LineAlias, ItemAdapter.ItemHolder> {


        public ItemAdapter(List<LineAlias> items) {
            super(items, R.layout.line_icon_layout);
        }

        @Override
        protected void bindValues(ItemHolder holder, LineAlias currentItem) {
            holder.title.setText(currentItem.lineDisplayName());
            holder.title.setTextColor(currentItem.textColor());
            holder.back.setColor(currentItem.backgroundColor());

            holder.view.post(() -> holder.view.setMinimumWidth(holder.view.getHeight()));
        }

        @Override
        protected ItemHolder createHolder(View view) {
            return new ItemHolder(view);
        }

        private static class ItemHolder extends ItemViewHolder {
            TextView title;
            View view;
            GradientDrawable back;

            public ItemHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.line_name);
                view = itemView.findViewById(R.id.icon_container);
                back = (GradientDrawable) view.getBackground();
            }
        }
    }


}
