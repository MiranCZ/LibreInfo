package com.example.mhdstuff.parsing.types.departure;

import com.example.mhdstuff.parsing.types.Vehicle;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class VehicleInfo {
    private Integer id;
    private Integer delay;

    public VehicleInfo(int id, int delay) {
        this.id = id;
        this.delay = delay;
    }

    public VehicleInfo() {
        id = null;
        delay = null;
    }

    public int getDelayColor() {
        return Vehicle.getDelayColor(delay());
    }

    public int id() {
        if (!hasId()) throw new NoSuchElementException();
        return id;
    }

    public boolean hasId() {
        return id != null;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int delay() {
        if (!hasDelay()) throw new NoSuchElementException();
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean hasDelay() {
        return delay != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (VehicleInfo) obj;
        return this.id == that.id &&
                this.delay == that.delay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, delay);
    }

    @Override
    public String toString() {
        return "VehicleInfo[" +
                "id=" + id + ", " +
                "delay=" + delay + ']';
    }


    public boolean hasBoth() {
        return hasId() && hasDelay();
    }
}
