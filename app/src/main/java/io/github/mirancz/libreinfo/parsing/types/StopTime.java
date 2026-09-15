package io.github.mirancz.libreinfo.parsing.types;

public record StopTime(Time arrival, Time departure) {


    public StopTime(Time time) {
        this(time, time);
    }

    public boolean immediateDeparture() {
        return arrival.equals(departure);
    }

}
