package io.github.mirancz.libreinfo.activity.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.mirancz.libreinfo.R;
import io.github.mirancz.libreinfo.activity.base.BaseActivity;
import io.github.mirancz.libreinfo.parsing.types.Vehicle;

public class VehicleInfoBottomSheet extends Fragment {

    private final Vehicle vehicle;
    private final BaseActivity parent;

    public VehicleInfoBottomSheet(Vehicle vehicle, BaseActivity parent) {
        this.vehicle = vehicle;
        this.parent = parent;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.vehicle_info_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        FrameLayout vehicleLineIcon = view.findViewById(R.id.vehicle_line_icon);
        TextView vehicleHeading = view.findViewById(R.id.vehicle_heading);

        View wheelchairIcon = view.findViewById(R.id.vehicle_wheelchair_icon);
        if (Boolean.TRUE.equals(vehicle.getLowFloor())) {
            wheelchairIcon.setVisibility(View.VISIBLE);
        } else {
            wheelchairIcon.setVisibility(View.INVISIBLE);
        }

        // TODO maybe cache this
        vehicleLineIcon.removeAllViews();
        vehicleLineIcon.addView(vehicle.getLine().createLineIconView(vehicleLineIcon, parent));

        vehicleHeading.setText(vehicle.getFinalStopText());

        TextView nextStop = view.findViewById(R.id.vehicle_next_stop);

        nextStop.setText(vehicle.getLastStop().name);

        TextView delayText = view.findViewById(R.id.vehicle_delay);

        if (vehicle.getDelay() == null) {
            delayText.setVisibility(View.GONE);
            return;
        }

        delayText.setVisibility(View.VISIBLE);
        delayText.setText(vehicle.getDelaySpan(parent));
    }
}
