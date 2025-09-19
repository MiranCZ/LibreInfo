package com.example.mhdstuff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.parsing.types.Stop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ButtonViewHolder> {

    private List<Stop> items;
    Map<Integer, ButtonViewHolder> holders = new HashMap<>();

    public ItemAdapter(List<Stop> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ButtonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonViewHolder holder, int position) {
        Stop currentItem = items.get(position);
        holder.button.setText(currentItem.name());

        holders.put(position, holder);
//        holderMap.put(currentItem, holder);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void submitList(List<Stop> list) {
        for (int i = 0; i < holders.size() && i < list.size(); i++) {
            if (!holders.containsKey(i)) break;
            holders.get(i).button.setText(list.get(i).name());
        }
        this.items = list;
    }

    static class ButtonViewHolder extends RecyclerView.ViewHolder {

        Button button;

        public ButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.item_button);
        }
    }
}