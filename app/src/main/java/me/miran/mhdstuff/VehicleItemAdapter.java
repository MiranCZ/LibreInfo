package me.miran.mhdstuff;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import me.miran.mhdstuff.activity.base.BaseActivity;
import me.miran.mhdstuff.activity.VehicleMapActivity;
import me.miran.mhdstuff.activity.listview.AbstractItemAdapter;
import me.miran.mhdstuff.parsing.types.Vehicle;

import java.util.List;

public class VehicleItemAdapter extends AbstractItemAdapter<Vehicle, VehicleItemAdapter.VehicleItemHolder> {


    private final BaseActivity parent;

    public VehicleItemAdapter(List<Vehicle> items, BaseActivity parent) {
        super(items, R.layout.vehicle_entry_layout);
        this.parent = parent;
    }

    @Override
    protected void bindValues(VehicleItemHolder holder, Vehicle item) {
        holder.vehicleNums.setText(item.getVehicleNumbersString());
        holder.vehicleService.setText(item.serviceId());

        if (item.lowFloor()) {
            holder.wheelchairIcon.setVisibility(View.VISIBLE);
        } else {
            holder.wheelchairIcon.setVisibility(View.INVISIBLE);
        }

        // TODO maybe cache this
        holder.vehicleLineIcon.removeAllViews();
        holder.vehicleLineIcon.addView(item.line().createLineIconView(holder.vehicleLineIcon, parent));

        holder.vehicleHeading.setText(item.getFinalStopText());
        holder.vehicleDelay.setText(item.getDelaySpan(parent));

        holder.vehicleNextStop.setText(item.lastStop().name);

        holder.layout.setOnClickListener(view -> {
            parent.startActivity(VehicleMapActivity.class, intent -> {
                intent.putExtra("following", item.id());
                intent.putExtra("lat", item.location().latitude());
                intent.putExtra("lng", item.location().longitude());
            });
        });
    }

    @Override
    protected VehicleItemHolder createHolder(View view) {
        return new VehicleItemHolder(view);
    }

    protected static class VehicleItemHolder extends ItemViewHolder {

        View layout;

        TextView vehicleNums;
        TextView vehicleService;
        FrameLayout wheelchairIcon;

        FrameLayout vehicleLineIcon;
        TextView vehicleHeading;
        TextView vehicleDelay;
        TextView vehicleNextStop;

        public VehicleItemHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.vehicle_entry);
            vehicleNums = itemView.findViewById(R.id.vehicle_nums);
            vehicleService = itemView.findViewById(R.id.vehicle_service);
            wheelchairIcon = itemView.findViewById(R.id.vehicle_wheelchair_icon);

            vehicleLineIcon = itemView.findViewById(R.id.vehicle_line_icon);
            vehicleHeading = itemView.findViewById(R.id.vehicle_heading);
            vehicleDelay = itemView.findViewById(R.id.vehicle_delay);

            vehicleNextStop = itemView.findViewById(R.id.vehicle_next_stop);
        }
    }

}
