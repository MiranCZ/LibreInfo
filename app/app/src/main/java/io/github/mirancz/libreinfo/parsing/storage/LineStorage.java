package io.github.mirancz.libreinfo.parsing.storage;

import android.graphics.Color;

import io.github.mirancz.libreinfo.codec.LinesCodec;
import io.github.mirancz.libreinfo.codec.util.AppInputStream;
import io.github.mirancz.libreinfo.exception.AppException;
import io.github.mirancz.libreinfo.model.LineAlias;
import io.github.mirancz.libreinfo.util.AppLog;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class LineStorage implements AppStorage {

    public static LineStorage parse(AppInputStream is) throws AppException {
        try {
            return new LineStorage(LinesCodec.read(is));
        } catch (IOException e) {
            throw AppException.dataLoad(e);
        }
    }


    private static String readColor(AppInputStream is) throws IOException {
        int r = is.read();
        int g = is.read();
        int b = is.read();

        return String.format("#%02X%02X%02X", r, g, b);
    }


    private final List<LineAlias> aliases;
    private final Map<Short, LineAlias> idToAlias = new HashMap<>();
    private final Map<String, LineAlias> nameToAlias = new HashMap<>();

    private LineStorage(List<LineAlias> aliases) {
        this.aliases = aliases;

        for (LineAlias alias : aliases) {
             idToAlias.put(alias.getId(), alias);
             nameToAlias.put(alias.getDisplayName(), alias);
        }

        Collections.sort(this.aliases);
    }

    public LineAlias getAlias(int id) {
        return getAlias((short) id);
    }

    public LineAlias getAlias(short id) {
        if (!idToAlias.containsKey(id)) {
            // this does actually happen sometimes due to the api not listing all line aliases
            // the original client deals with it by just ignoring the entry, I think this is at least a bit better
            AppLog.w("Vehicle with id "+id+" not found! Creating a dummy one...");

            LineAlias dummy = new LineAlias(id, String.valueOf(id), android.graphics.Color.MAGENTA, Color.WHITE);
            idToAlias.put(id, dummy);
            nameToAlias.put(dummy.getDisplayName(), dummy);
        }

        return idToAlias.get(id);
    }

    public LineAlias getAlias(String name) {
        LineAlias alias = nameToAlias.get(name);
        if (alias == null) return new LineAlias(0, name, android.graphics.Color.MAGENTA, Color.WHITE);
        return alias;
    }

    public Optional<LineAlias> getOptionalAlias(String name) {
        return Optional.ofNullable(nameToAlias.get(name));
    }

    public List<LineAlias> getAllAliases() {
        return aliases;
    }
}
