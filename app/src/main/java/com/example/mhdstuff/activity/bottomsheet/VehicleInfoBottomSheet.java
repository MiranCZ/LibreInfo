package com.example.mhdstuff.activity.bottomsheet;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.parsing.types.MapVehicle;
import com.example.mhdstuff.util.DelayUtil;

public class VehicleInfoBottomSheet extends Fragment {

    private final MapVehicle vehicle;
    private final BaseActivity parent;

    public VehicleInfoBottomSheet(MapVehicle vehicle, BaseActivity parent) {
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
        if (false) { //TODO
            wheelchairIcon.setVisibility(View.VISIBLE);
        } else {
            wheelchairIcon.setVisibility(View.INVISIBLE);
        }

        // TODO maybe cache this
        vehicleLineIcon.removeAllViews();
        vehicleLineIcon.addView(vehicle.line().createLineIconView(vehicleLineIcon, parent));

        vehicleHeading.setText(vehicle.finalStop().name);

        TextView nextStop = view.findViewById(R.id.vehicle_next_stop);

        nextStop.setText(vehicle.prevStop().name);

        TextView delayText = view.findViewById(R.id.vehicle_delay);

        int delay = vehicle.delay();

        // FIXME hardcoded ughhh
        SpannableString span = new SpannableString(DelayUtil.getDelayText(parent, delay));
        span.setSpan(new ForegroundColorSpan(DelayUtil.getDelayColor(delay)), 0, span.length(), 0);

        delayText.setText(span);
    }
}
