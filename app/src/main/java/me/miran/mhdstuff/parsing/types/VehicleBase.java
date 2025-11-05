package me.miran.mhdstuff.parsing.types;


/**
 * Here so {@link MapVehicle} and {@link Vehicle} can be used as one.
 */
//TODO might want to rename this
public interface VehicleBase {


    LineAlias line();

    Location location();

    int bearing();

    int id();

    Stop finalStop();

}
