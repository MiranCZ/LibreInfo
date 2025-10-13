package com.example.mhdstuff.parsing.types;

public record Trip(short serviceId, short lineId, int headsignId, int startPos, byte length) {
}
