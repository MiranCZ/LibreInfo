package com.example.mhdstuff.parsing.types;

import com.google.gson.JsonObject;

public record LineAlias(int id, String lineDisplayName, Color backgroundColor, Color textColor) {

    public static LineAlias parse(JsonObject obj) {
        if (obj == null) return null;

        int id = obj.get("LineID").getAsInt();
        String displayName = obj.get("LineName").getAsString();
        // TODO parse colors

        return new LineAlias(id, displayName, null, null);
    }

    public TransportLine toTransportLine() {
        return new TransportLine(id, lineDisplayName);
    }
}
