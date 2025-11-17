package me.miran.mhdstuff.raptor;

import java.util.List;

import me.miran.mhdstuff.parsing.types.Time;

public record Path(List<PathNode> nodes) {


    public Time getDeparture() {
        return nodes.get(0).departureTime();
    }

}
