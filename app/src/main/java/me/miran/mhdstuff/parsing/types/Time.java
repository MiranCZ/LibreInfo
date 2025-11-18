package me.miran.mhdstuff.parsing.types;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalTime;

public record Time(byte hours, byte minutes) implements Comparable<Time>, Parcelable {

    public static final Time INF = new Time(Byte.MAX_VALUE, Byte.MAX_VALUE);
    public static final Time ZERO = new Time(0, 0);
    public static final Time MIN = new Time(Byte.MIN_VALUE, Byte.MIN_VALUE);

    public static final Creator<Time> CREATOR = new Creator<>() {
        @Override
        public Time createFromParcel(Parcel in) {
            byte hours = in.readByte();
            byte minutes = in.readByte();

            return new Time(hours, minutes);
        }

        @Override
        public Time[] newArray(int size) {
            return new Time[size];
        }
    };

    public Time(int hours, int minutes) {
        this((byte)hours,(byte) minutes);

        if (hours > Byte.MAX_VALUE || hours < Byte.MIN_VALUE || minutes > Byte.MAX_VALUE || minutes < Byte.MIN_VALUE) {
            throw new IllegalArgumentException("Overflow");
        }
    }

    public static Time parse(String str) {
        String[] parts = str.split(":");

        return new Time(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    public static Time now() {
        LocalTime now = LocalTime.now();

        return new Time((byte) now.getHour(), (byte) now.getMinute());
    }

    @Override
    public int compareTo(Time o) {
        if (hours == o.hours) {
            return minutes - o.minutes;
        }
        return hours - o.hours;
    }

    public boolean isLower(Time other) {
        return compareTo(other) < 0;
    }

    public boolean isHigher(Time other) {
        return compareTo(other) > 0;
    }

    public Time addMinutes(int mins) {
        int newMinutes = minutes+mins;

        return new Time((byte) (hours+newMinutes/60), (byte) (newMinutes%60));
    }

    public int getMinsDiff(Time other) {
        return (hours - other.hours) * 60 + (minutes - other.minutes);
    }

    public short toCompactShort() {
        return (short) ((hours*60) + minutes);
    }

    public String formatRemaining() {
        Time now = now();

        int mins = (hours - now.hours)*60 + (minutes - now.minutes);

        return mins +" min";
    }

    public String format() {
        String s = (hours % 24)+"";
        if (s.length() != 2) s = "0"+s;
        String m = minutes+"";
        if (m.length() != 2) m = "0"+m;

        return s + ":" +m;
    }


    public Time plusMinutes(int add) {
        int newMinutes = minutes+add;

        return new Time(hours + newMinutes/60, newMinutes % 60);
    }

    public boolean isAfter(Time time) {
        return getMinsDiff(time) > 0;
    }

    public boolean isBefore(Time time) {
        return getMinsDiff(time) < 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeByte(hours);
        dest.writeByte(minutes);
    }
}