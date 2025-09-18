package com.example.mhdstuff.parsing.types;

import android.graphics.Color;

import com.google.gson.JsonObject;

public record LineAlias(int id, String lineDisplayName, int backgroundColor, int textColor) {

    public static LineAlias parse(JsonObject obj) {
        if (obj == null) return null;

        int id = obj.get("LineID").getAsInt();
        String displayName = obj.get("LineName").getAsString();
        // TODO parse colors
        int background = Color.parseColor(obj.get("Color").getAsString());
        int textColor = Color.parseColor(obj.get("TextColor").getAsString());

        return new LineAlias(id, displayName, background, textColor);
    }

    public TransportLine toTransportLine() {
        return new TransportLine(id, lineDisplayName);
    }
}
