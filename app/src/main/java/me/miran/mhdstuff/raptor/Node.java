package me.miran.mhdstuff.raptor;

import me.miran.mhdstuff.parsing.types.RouteStop;
import me.miran.mhdstuff.parsing.types.Time;

/**
 *
 * @param parent Node from which we continue
 * @param tripId The trip we are taking
 * @param leaveStop at what stop we leave
 * @param enterStop from what stop we are going
 * @param leaveTime the time we depart from the route
 * @param enterTime the time we enter the route
 */
public record Node(Node parent, int transferTime, int tripId, RouteStop leaveStop, short enterStop, Time leaveTime, Time enterTime, double cost) {


    @Override
    public String toString() {
        return "Node{" +
                "parent=" + parent +
                ", fromStop=" + enterStop +
                '}';
    }
}
