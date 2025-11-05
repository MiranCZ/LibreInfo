package com.example.mhdstuff.parsing.types;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.util.IOUtil;
import com.example.mhdstuff.util.PreferencesHolder;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Stop implements Parcelable {


    public static Stop NONE = new Stop(-1, "UNKNOWN", Location.NONE, PreferencesHolder.NONE);

    public final int id;
    public final String name;
    public final Location location;
    private final PreferencesHolder favStops;

    private boolean favourite;

    public Stop(int id, String name, Location location, PreferencesHolder favStops) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.favStops = favStops;
        this.favourite = favStops.getBoolean(id, false);
    }

    public static final Creator<Stop> CREATOR = new Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel in) {
            int id = in.readInt();
            return IdStorage.getStopStorageOrThrow().getStop(id);
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };

    public static List<Stop> parseStops(DataInputStream is, PreferencesHolder favStops) throws IOException {
        List<Stop> result = new ArrayList<>();

        while (is.readBoolean()) {
            result.add(parse(is, favStops));
        }

        return result;
    }

    public static Stop parse(DataInputStream is, PreferencesHolder favStops) throws IOException {
        int stopId = is.readInt();

        int nameLen = is.readInt();

        byte[] result = IOUtil.readNBytes(is, nameLen);

        String name = new String(result, StandardCharsets.UTF_8);

        double lat = is.readDouble();
        double lon = is.readDouble();

        // TODO set favourite
        return new Stop(stopId, name, new Location(lat, lon), favStops);
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
        favStops.putBoolean(id, favourite);
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
        dest.writeInt(id);
    }
}
