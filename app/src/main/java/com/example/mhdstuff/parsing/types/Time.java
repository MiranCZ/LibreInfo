package com.example.mhdstuff.parsing.types;

import java.io.DataInputStream;
import java.io.IOException;
import java.time.LocalTime;

public record Time(byte hours, byte minutes) implements Comparable<Time> {

    public static Time now() {
        LocalTime now = LocalTime.now();

        return new Time((byte) now.getHour(), (byte) now.getMinute());
    }

    @Override
    public int compareTo(Time o) {
        int hourRes = Integer.compare(hours, o.hours);

        if (hourRes == 0) {
            return Integer.compare(minutes, o.minutes);
        }
        return hourRes;
    }

    public int getMinsDiff(Time other) {
        return (hours - other.hours) * 60 + (minutes - other.minutes);
    }

    public String format() {
        Time now = now();

        int mins = (hours - now.hours)*60 + (minutes - now.minutes);

        return mins +" min";
    }


}