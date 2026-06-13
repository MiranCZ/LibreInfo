package me.miran.libreinfo.parsing.types.stop;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import me.miran.libreinfo.parsing.storage.IdStorage;
import me.miran.libreinfo.parsing.storage.StopMapper;
import me.miran.libreinfo.parsing.types.Location;
import me.miran.libreinfo.util.AppInputStream;
import me.miran.libreinfo.util.PreferencesHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Stop implements Parcelable {


    public static Stop NONE = new Stop(StopId.NONE, "UNKNOWN", "UNKNOWN", Location.NONE, PreferencesHolder.NONE);

    public final StopId id;
    public final String name;
    public final String parentStation;
    public final Location location;
    private final PreferencesHolder favStops;

    private boolean favourite;

    public Stop(StopId id, String name, String parentStation, Location location, PreferencesHolder favStops) {
        this.id = id;
        this.name = name;
        this.parentStation = parentStation;
        this.location = location;
        this.favStops = favStops;
        this.favourite = favStops.getBoolean(id.internal(), false);
    }

    public static final Creator<Stop> CREATOR = new Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel in) {
            int id = in.readInt();
            return IdStorage.getStopStorageOrBlock().getStop(StopId.internal(id));
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };

    public static List<Stop> parseStops(AppInputStream is, PreferencesHolder favStops, StopMapper mapper) throws IOException {
        List<Stop> result = new ArrayList<>();

        while (is.readBoolean()) {
            result.add(parse(is, favStops, mapper));
        }

        return result;
    }

    public static Stop parse(AppInputStream is, PreferencesHolder favStops, StopMapper mapper) throws IOException {
        int stopId = is.readInt();

        String name = is.readString();
        String parentStation = is.readString();

        double lat = is.readDouble();
        double lon = is.readDouble();

        StopId id = new StopId(stopId,mapper.getOriginal(stopId));

        // TODO set favourite
        return new Stop(id, name, parentStation, new Location(lat, lon), favStops);
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
        favStops.putBoolean(id.internal(), favourite);
        System.out.println("CHANGED VAOURITE TO "+favourite);
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void flush() {
        favStops.flush();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Stop) obj;
        return this.id == that.id &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, location);
    }

    @Override
    public String toString() {
        return "Stop[" +
                "id=" + id + ", " +
                "name=" + name + ", " +
                "location=" + location + ']';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id.internal());
    }
}
