package com.example.mhdstuff;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.mhdstuff.activity.listview.AbstractItemAdapter;
import com.example.mhdstuff.parsing.types.Vehicle;

import org.w3c.dom.Text;

import java.util.List;
import java.util.stream.Collectors;

public class VehicleItemAdapter extends AbstractItemAdapter<Vehicle, VehicleItemAdapter.VehicleItemHolder> {


    private final Context context;

    public VehicleItemAdapter(List<Vehicle> items, Context context) {
        super(items, R.layout.vehicle_entry_layout);
        this.context = context;
    }

    @Override
    protected void bindValues(VehicleItemHolder holder, Vehicle item) {
        holder.vehicleNums.setText(item.getVehicleNumbersString());
        holder.vehicleService.setText(item.getServiceString());

        if (item.lowFloor()) {
            holder.wheelchairIcon.setVisibility(View.VISIBLE);
        } else {
            holder.wheelchairIcon.setVisibility(View.INVISIBLE);
        }

        // TODO maybe cache this
        holder.vehicleLineIcon.removeAllViews();
        holder.vehicleLineIcon.addView(item.line().createLineIconView(holder.vehicleLineIcon, context));

        holder.vehicleHeading.setText(item.getFinalStopText());
        holder.vehicleDelay.setText(item.getDelaySpan());

        holder.vehicleNextStop.setText(item.lastStop().name());
    }

    @Override
    protected VehicleItemHolder createHolder(View view) {
        return new VehicleItemHolder(view);
    }

    protected static class VehicleItemHolder extends ItemViewHolder {

        TextView vehicleNums;
        TextView vehicleService;
        FrameLayout wheelchairIcon;

        FrameLayout vehicleLineIcon;
        TextView vehicleHeading;
        TextView vehicleDelay;
        TextView vehicleNextStop;

        public VehicleItemHolder(@NonNull View itemView) {
            super(itemView);
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
