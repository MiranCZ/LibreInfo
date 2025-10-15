package com.example.mhdstuff.parsing.types;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record TimeMark(StopTime stopTime, boolean certain, boolean leaving) {


    public String getFormattedString(int minutesThreshold, boolean includeDelay) {
        Time time = stopTime.getDeparture(includeDelay);

        if (leaving) return "**";

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

    public Time time() {
        return stopTime.getDeparture();
    }

}
