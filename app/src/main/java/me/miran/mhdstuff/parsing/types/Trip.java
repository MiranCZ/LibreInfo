package me.miran.mhdstuff.parsing.types;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import me.miran.mhdstuff.parsing.storage.RouteStopStorage;

public record Trip(int id, short serviceId, short lineId, int headsignId, short blockId, boolean lowFloor, int startPos, byte length) implements Parcelable {

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel source) {
            return new Trip(source.readInt(),
                    (short) source.readInt(),
                    (short) source.readInt(),
                    source.readInt(),
                    (short) source.readInt(),
                    source.readBoolean(),
                    source.readInt(),
                    source.readByte()
            );
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };


    public RouteStop[] getRouteStops(RouteStopStorage storage) {
        return storage.getRouteStopsFromSegmentParsed(startPos, length);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(serviceId);
        dest.writeInt(lineId);
        dest.writeInt(headsignId);
        dest.writeInt(blockId);
        dest.writeBoolean(lowFloor);
        dest.writeInt(startPos);
        dest.writeByte(length);
    }
}
