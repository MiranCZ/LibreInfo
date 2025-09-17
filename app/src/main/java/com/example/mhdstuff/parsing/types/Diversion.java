package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public record Diversion(int id, String number, String title, Location location, Time from, Time to,
                        boolean valid, String publicText, String privateText, String type,
                        List<TransportLine> lines) {

    public static List<Diversion> parseDiversions(JsonArray array, LineStorage storage) {
        return TypeHelper.parseList(array, (o) -> parse(o, storage));
    }

    public static Diversion parse(JsonObject obj, LineStorage storage) {
        if (obj == null) return null;

        int id = obj.get("DiversionID").getAsInt();
        String number = obj.get("Number").getAsString();
        String title = obj.get("Title").getAsString();
        Location location = Location.parse(obj);
        Time from = Time.parse(obj.get("ValidFrom").getAsString());
        Time to = Time.parse(obj.get("ValidTo").getAsString());

        boolean valid = obj.get("IsValid").getAsBoolean();
        String publicText = obj.get("PublicText").getAsString();
        String privateText = obj.get("PrivateText").getAsString();
        String type = obj.get("Type").getAsString(); //TODO figure out possible types and create an ENUM?
        List<TransportLine> lines = TransportLine.parseTransportLines(
                obj.get("AffectedLines").getAsString(), storage
        );

        return new Diversion(id, number, title, location, from, to, valid, publicText, privateText, type, lines);
    }
}
