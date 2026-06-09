package me.miran.libreinfo.parsing.types;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import me.miran.libreinfo.util.DelayUtil;

public class StopTime {


    private final Time departure;
    private final Time arrival;
    private int delay = 0;

    public StopTime(Time time) {
        this(time, time);
    }

    public StopTime(Time arrival, Time departure) {
        this.departure = departure;
        this.arrival = arrival;
    }

    public SpannableString formatColoredDelay(boolean canIncludeBoth) {
        if (immediateDeparture() || !canIncludeBoth) {
            String str = arrival.addMinutes(delay).format();

            if (delay != 0) {
                str = "("+delay+") "+str;
            }
            SpannableString span = new SpannableString(str);

            int color = DelayUtil.getDelayColor(delay);
            span.setSpan(new ForegroundColorSpan(color), 0, span.length(), 0);

            return span;
        }

        String first = arrival.addMinutes(delay).format();

        int loweredDelay = getLoweredDelay();

        String second = departure.addMinutes(loweredDelay).format();

        SpannableString span = new SpannableString(first + " - " + second);

        span.setSpan(new ForegroundColorSpan(DelayUtil.getDelayColor(delay)), 0, first.length(), 0);
        span.setSpan(new ForegroundColorSpan(DelayUtil.getDelayColor(loweredDelay)), first.length() + 3, span.length(), 0);

        return span;
    }

    public String formatWithoutDelay(boolean canIncludeBoth) {
        if (immediateDeparture() || !canIncludeBoth) return arrival.format();

        return arrival.format() + " - " + departure.format();
    }



    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    public int getLoweredDelay() {
        int diff = departure.getMinsDiff(arrival);

        return Math.max(0, delay - diff);
    }

    public Time getDeparture(boolean includeDelay) {
        if (!includeDelay) return departure;

        return getDeparture();
    }

    public Time getDeparture() {
        return departure.addMinutes(getLoweredDelay());
    }

    public Time getArrival() {
        return arrival.addMinutes(delay);
    }

    public Time getArrival(boolean includeDelay) {
        if (!includeDelay) return arrival;

        return getArrival();
    }

    public boolean immediateDeparture() {
        return arrival.equals(departure);
    }

}
