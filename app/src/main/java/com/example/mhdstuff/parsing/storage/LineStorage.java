package com.example.mhdstuff.parsing.storage;

import com.example.mhdstuff.parsing.types.Color;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.TransportLine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO
public class LineStorage {

    public static LineStorage parse(JsonArray array) {
        List<LineAlias> aliases = new ArrayList<>();

        for (JsonElement element : array) {
            aliases.add(LineAlias.parse(element.getAsJsonObject()));
        }

        return new LineStorage(aliases);
    }


    private final List<LineAlias> aliases;
    private final Map<Integer, LineAlias> idToAlias = new HashMap<>();
    private final Map<String, LineAlias> nameToAlias = new HashMap<>();

    private LineStorage(List<LineAlias> aliases) {
        this.aliases = aliases;

        for (LineAlias alias : aliases) {
             idToAlias.put(alias.id(), alias);
             nameToAlias.put(alias.lineDisplayName(), alias);
        }
    }

    public LineAlias getAlias(int id) {
        if (!idToAlias.containsKey(id)) {
            // this does actually happen sometimes due to the api not listing all line aliases
            // the original client deals with it by just ignoring the entry, I think this is at least a bit better
            System.out.println("[WARN] Vehicle with id "+id+" not found! Creating a dummy one...");

            LineAlias dummy = new LineAlias(id, String.valueOf(id), Color.PINK, Color.WHITE);
            idToAlias.put(id, dummy);
            nameToAlias.put(dummy.lineDisplayName(), dummy);
        }

        return idToAlias.get(id);
    }

    public LineAlias getAlias(String name) {
        return nameToAlias.get(name);
    }

    public TransportLine getLine(int id) {
        return getAlias(id).toTransportLine();
    }

    public TransportLine getLine(String name) {
        return getAlias(name).toTransportLine();
    }

}
