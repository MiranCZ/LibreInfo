package me.miran.mhdstuff.raptor;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import me.miran.mhdstuff.R;
import me.miran.mhdstuff.activity.base.BaseActivity;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.types.LineAlias;
import me.miran.mhdstuff.parsing.types.Stop;
import me.miran.mhdstuff.parsing.types.Time;
import me.miran.mhdstuff.parsing.types.Trip;

public record PathNode(Trip trip, Stop fromStop, Time departureTime, Stop toStop, Time arrivalTime,
                       int transferTime) implements Parcelable {

    public static final Creator<PathNode> CREATOR = new Creator<>() {
        @Override
        public PathNode createFromParcel(Parcel source) {
            return new PathNode(
                    source.readParcelable(Trip.class.getClassLoader()),
                    source.readParcelable(Stop.class.getClassLoader()),
                    source.readParcelable(Time.class.getClassLoader()),
                    source.readParcelable(Stop.class.getClassLoader()),
                    source.readParcelable(Time.class.getClassLoader()),
                    source.readInt()
            );
        }

        @Override
        public PathNode[] newArray(int size) {
            return new PathNode[size];
        }
    };

    public View createView(IdStorage storage, ViewGroup parent, BaseActivity context) {
        LineAlias line = storage.lineStorage().getAlias(trip.lineId());

        View view = LayoutInflater.from(context).inflate(R.layout.connection_node_layout, parent, false);

        FrameLayout lineIcon = view.findViewById(R.id.vehicle_line_icon);
        View lineIconView = line.createLineIconView(lineIcon, context);

        lineIcon.addView(lineIconView);

        TextView heading = view.findViewById(R.id.vehicle_heading);
        heading.setText(storage.tripStorage().getTripHeadsign(trip));

        TextView fromTime = view.findViewById(R.id.time_from);
        fromTime.setText(departureTime.format());

        TextView fromStopText = view.findViewById(R.id.stop_from);
        fromStopText.setText(fromStop.name);


        TextView toTime = view.findViewById(R.id.time_to);
        toTime.setText(arrivalTime.format());

        TextView toStopText = view.findViewById(R.id.stop_to);
        toStopText.setText(toStop.name);

        return view;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(trip, flags);
        dest.writeParcelable(fromStop, flags);
        dest.writeParcelable(departureTime, flags);
        dest.writeParcelable(toStop, flags);
        dest.writeParcelable(arrivalTime, flags);
        dest.writeInt(transferTime);
    }
}
