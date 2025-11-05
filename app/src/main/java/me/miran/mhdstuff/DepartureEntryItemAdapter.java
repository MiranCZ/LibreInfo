package me.miran.mhdstuff;

import android.view.View;

import androidx.annotation.NonNull;

import me.miran.mhdstuff.activity.base.BaseActivity;
import me.miran.mhdstuff.activity.listview.AbstractItemAdapter;
import me.miran.mhdstuff.parsing.storage.ApiStorage;
import me.miran.mhdstuff.parsing.types.Time;
import me.miran.mhdstuff.parsing.types.departure.DepartureEntry;
import me.miran.mhdstuff.util.Pair;
import com.google.gson.JsonObject;

import java.util.List;

public class DepartureEntryItemAdapter extends AbstractItemAdapter<DepartureEntry, DepartureEntryItemAdapter.DepartureEntryHolder> {


    private final BaseActivity parent;
    private final ApiStorage apiStorage;
    private final JsonObject stopDelays;

    public DepartureEntryItemAdapter(BaseActivity parent, List<DepartureEntry> items, ApiStorage apiStorage, JsonObject stopDelays) {
        super(items, R.layout.departure_entry_layout);
        this.parent = parent;
        this.apiStorage = apiStorage;
        this.stopDelays = stopDelays;
    }

    @Override
    protected void bindValues(DepartureEntryHolder holder, DepartureEntry item) {
        boolean alreadyLeft = item.timeMark().time().isBefore(Time.now()) && !item.timeMark().leaving();

        Pair<Integer, Integer> lineRoute = apiStorage.getLineIdAndRoute(item.tripId());

        String lineId = lineRoute.left().toString();
        String routeId = lineRoute.right().toString();

        boolean showDelay = !alreadyLeft;
        if (alreadyLeft) {
            int delay = -1;
            if (stopDelays.has(lineId)) {
                JsonObject delays = stopDelays.getAsJsonObject(lineId);

                if (delays.has(routeId)) {
                    delay = delays.getAsJsonObject(routeId).get("delay").getAsInt();
                }
            }
            item.vehicleInfo().setDelay(delay);

            showDelay = delay != -1;
        }

        item.populateDepartureViewEntry(parent, parent, holder.itemView, showDelay);

        float alpha = 1;
        if (alreadyLeft) {
            // TODO sync alpha across TripDetail
            alpha = 0.35f;
        }

        holder.itemView.findViewById(R.id.departure_heading).setAlpha(alpha);
        holder.itemView.findViewById(R.id.departure_arrival).setAlpha(alpha);
    }

    @Override
    protected DepartureEntryHolder createHolder(View view) {
        return new DepartureEntryHolder(view);
    }

    protected static class DepartureEntryHolder extends ItemViewHolder {

        public DepartureEntryHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
