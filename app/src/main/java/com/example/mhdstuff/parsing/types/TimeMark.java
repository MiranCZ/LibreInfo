package com.example.mhdstuff.parsing.types;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record TimeMark(LocalTime time, boolean certain) {

    public TimeMark(LocalTime time) {
        this(time, true);
    }

    public static TimeMark parse(String str) {
        if (str == null) return null;
        if (str.equals("**")) {
            return new TimeMark(LocalTime.MIN);
        }

        if (str.endsWith("min")) {
            str = str.substring(0, str.length()-"min".length()).strip();

            int minutes = Integer.parseInt(str);
            LocalTime now = LocalTime.now();

            return new TimeMark(now.plusMinutes(minutes));
        } else {
            return new TimeMark(LocalTime.parse(str, DateTimeFormatter.ofPattern("HH:mm")));
        }
    }

    public String getFormattedString(int minutesThreshold) {
        if (time == LocalTime.MIN) return "**";

        LocalTime now = LocalTime.now();
        long elapsed = Duration.between(now, time).toMinutes();
        if (elapsed < 0) {
            elapsed += Duration.ofDays(1).toMinutes();
        }

        String prefix = certain ? "" : "± ";

        if (elapsed < minutesThreshold) {
            return prefix + elapsed + " min";
        } else {
            return prefix + time.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

    public boolean isLeaving() {
        return time == LocalTime.MIN;
    }

}
