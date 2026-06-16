package me.miran.libreinfo.parsing.types.connection;

public abstract class TransportMode {


    public static TransportMode vehicle(int tripId) {
        return new VehicleTransport(tripId);
    }

    public static TransportMode walk(int distanceMetres) {
        return new WalkTransport(distanceMetres);
    }


    public boolean isWalk() {
        return this instanceof WalkTransport;
    }

    public boolean isVehicle() {
        return this instanceof VehicleTransport;
    }

    public int getTripId() {
        if (this instanceof VehicleTransport vehicleTransport) {
            return vehicleTransport.tripId;
        }

        throw new IllegalStateException("Not a vehicle transport!");
    }

    public int getDistance() {
        if (this instanceof WalkTransport walkTransport) {
            return walkTransport.distanceMetres;
        }

        throw new IllegalStateException("Not walk transport!");
    }

    private static class VehicleTransport extends TransportMode {
        private final int tripId;

        private VehicleTransport(int tripId) {
            this.tripId = tripId;
        }
    }

    private static class WalkTransport extends TransportMode {
        private final int distanceMetres;

        private WalkTransport(int distanceMetres) {
            this.distanceMetres = distanceMetres;
        }

    }

}
