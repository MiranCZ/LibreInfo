package com.example.mhdstuff.parsing.types;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record TimeMark(Time time, boolean certain) {

    public TimeMark(Time time) {
        this(time, true);
    }

    public static TimeMark parse(String str) {
        if (str == null) return null;
        if (str.equals("**")) {
            return new TimeMark(Time.MIN);
        }

        if (str.endsWith("min")) {
            str = str.substring(0, str.length()-"min".length()).strip();

            int minutes = Integer.parseInt(str);
            Time now = Time.now();

            return new TimeMark(now.plusMinutes(minutes));
        } else {
            return new TimeMark(Time.parse(str));
        }
    }

    public String getFormattedString(int minutesThreshold) {
        if (time == Time.MIN) return "**";

        Time now = Time.now();
        long elapsed = time.getMinsDiff(now);
        if (elapsed < 0) {
            elapsed += Duration.ofDays(1).toMinutes();
        }

        if (elapsed < minutesThreshold) {
            String prefix = certain ? "" : "± ";
            return prefix + elapsed + " min";
        } else {
            // do not add "uncertain" prefix for connections far away
            return time.format();
        }
    }

    public boolean isLeaving() {
        return time == Time.MIN;
    }

}
