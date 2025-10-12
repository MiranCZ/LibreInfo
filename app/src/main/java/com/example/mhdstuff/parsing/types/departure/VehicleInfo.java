package com.example.mhdstuff.parsing.types.departure;

import com.example.mhdstuff.parsing.types.Vehicle;

public record VehicleInfo(int id, int delay) {

    public int getDelayColor() {
        return Vehicle.getDelayColor(delay);
    }

}
