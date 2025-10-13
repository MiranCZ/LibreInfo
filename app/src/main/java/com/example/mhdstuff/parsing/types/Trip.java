package com.example.mhdstuff.parsing.types;

public record Trip(short serviceId, short lineId, int headsignId, boolean lowFloor, int startPos, byte length) {
}
