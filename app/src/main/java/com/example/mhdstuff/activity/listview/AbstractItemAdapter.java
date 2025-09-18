package com.example.mhdstuff.activity.listview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.DiversionsItemAdapter;
import com.example.mhdstuff.R;

import java.util.List;

public abstract class AbstractItemAdapter<T, H extends AbstractItemAdapter.ItemViewHolder> extends RecyclerView.Adapter<H> {


    private final List<T> items;
    private final int layoutId;

    public AbstractItemAdapter(List<T> items, int layoutId) {
        this.items = items;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return createHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull H holder, int position) {
        T currentItem = items.get(position);

        bindValues(holder, currentItem);
    }


    protected abstract void bindValues(H holder, T item);

    protected abstract H createHolder(View view);

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected abstract static class ItemViewHolder extends RecyclerView.ViewHolder {

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }


    }

}
