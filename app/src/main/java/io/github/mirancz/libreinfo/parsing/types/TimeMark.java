package io.github.mirancz.libreinfo.parsing.types;

import androidx.annotation.Nullable;

import java.time.Duration;
import java.util.Objects;

public final class TimeMark {
    private final StopTime time;
    private final TimeMode mode;

    // I know this is kinda stupid to have it mutable but ughhhh
    @Nullable
    public Integer delay;
    public final boolean leaving;

    public TimeMark(StopTime time, TimeMode mode, @Nullable Integer delay, boolean leaving) {
        this.time = time;
        this.mode = mode;
        this.delay = delay;
        this.leaving = leaving;
    }


    public String getFormattedDepartureString(int minutesThreshold, boolean includeDelay) {
        Time time;

        if (includeDelay) {
            time = getDelayedDeparture();
        } else {
            time = getDeparture();
        }


        if (leaving) return "**";

        Time now = Time.now();
        long elapsed = time.getMinsDiff(now);
        if (elapsed < 0) {
            elapsed += Duration.ofDays(1).toMinutes();
        }

        if (elapsed < minutesThreshold) {
            String prefix = (delay != null) ? "" : "± ";
            return prefix + elapsed + " min";
        } else {
            // do not add "uncertain" prefix for connections far away
            return time.format();
        }
    }

    // FIXME somehow make clear it does NOT render delay
    public String getFormattedString() {
        if (immediateDeparture()) return getArrival().format();

        return getArrival().format() + " - " + getDeparture().format();
    }

    public Time getArrival() {
        return time.arrival();
    }

    public Time getDeparture() {
        if (mode == TimeMode.NORMAL) {
            return time.departure();
        } else if (mode == TimeMode.TERMINUS) {
            return time.arrival();
        }

        throw new IllegalStateException();
    }

    public Time getDelayedArrival() {
        return time.arrival().addMinutes(normalizedDelay());
    }

    public Time getDelayedDeparture() {
        if (mode == TimeMode.NORMAL) {
            int loweredDelay = getLoweredDelay();

            return time.departure().addMinutes(loweredDelay);
        } else if (mode == TimeMode.TERMINUS) {
            return time.arrival().addMinutes(normalizedDelay());
        }

        throw new IllegalStateException();
    }

    public boolean immediateDeparture() {
        return getArrival().equals(getDeparture());
    }

    public int getLoweredDelay() {
        int diff = time.departure().getMinsDiff(time.arrival());

        return Math.max(0, normalizedDelay() - diff);
    }

    private int normalizedDelay() {
        if (delay != null) return delay;
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TimeMark) obj;
        return Objects.equals(this.time, that.time) &&
                Objects.equals(this.delay, that.delay) &&
                this.leaving == that.leaving;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, mode, delay, leaving);
    }

    @Override
    public String toString() {
        return "TimeMark{" +
                "time=" + time +
                ", mode=" + mode +
                ", delay=" + delay +
                ", leaving=" + leaving +
                '}';
    }

    public enum TimeMode {
        /**
         * Arrivals are calculated with delay, departures get delay lowered if there is a waiting time from arrival
         */
        NORMAL,
        /**
         * At terminal stops only ARRIVAL is ever calculated with since departure for a terminus tells nothing
         */
        TERMINUS
    }


}
