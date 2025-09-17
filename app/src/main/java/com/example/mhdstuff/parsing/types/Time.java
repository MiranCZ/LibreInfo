package com.example.mhdstuff.parsing.types;


import androidx.annotation.NonNull;

import java.util.Locale;

public record Time(int day, int month, int year, int hours, int minutes) {

    public static final Time NONE = new Time(-1, -1, -1, -1, -1);

    /**
     * The format seems to be {@code DD.MM.YYYY HH:mm}
     */
    public static Time parse(String str) {
        if (str == null) return null;

        // sometimes time can be undefined in arbitrary ways :)
        if (str.isBlank() || str.equals("?")) {
            return NONE;
        }

        String time = str.substring(str.length()-5);
        String[] partsTime = time.strip().split(":");
        int hours = Integer.parseInt(partsTime[0].strip());
        int minutes = Integer.parseInt(partsTime[1].strip());

        str = str.substring(0, str.length()-6);
        String[] partsDate = str.strip().split("\\.");

        int day = Integer.parseInt(partsDate[0].strip());
        int month = Integer.parseInt(partsDate[1].strip());
        int year = Integer.parseInt(partsDate[2].strip());

        return new Time(day, month, year, hours, minutes);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),"%02d.%02d.%d %02d:%02d", day, month, year, hours, minutes);
    }
}
