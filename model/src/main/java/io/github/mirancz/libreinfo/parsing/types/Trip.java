package io.github.mirancz.libreinfo.parsing.types;


public record Trip(int id, short serviceId, short lineId, int headsignId, short blockId, boolean lowFloor, int startPos, byte length) {

}
