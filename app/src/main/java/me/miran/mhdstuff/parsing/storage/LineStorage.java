package me.miran.mhdstuff.parsing.storage;

import android.graphics.Color;

import me.miran.mhdstuff.parsing.types.LineAlias;
import me.miran.mhdstuff.util.IOUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class LineStorage {

    public static LineStorage parse(DataInputStream is) {
        List<LineAlias> aliases = new ArrayList<>();

        try(is) {
            while (is.readBoolean()) {
                int routeId = is.readInt();

                int nameLen = is.readInt();
                byte[] result = IOUtil.readNBytes(is, nameLen);

                String name = new String(result, StandardCharsets.UTF_8);

                String background = readColor(is);
                String text = readColor(is);

                aliases.add(new LineAlias(routeId, name, Color.parseColor(background), background, Color.parseColor(text), text));
            }

            return new LineStorage(aliases);
        } catch (IOException e) {
            e.printStackTrace();
            return new LineStorage(List.of());
        }
    }


    private static String readColor(DataInputStream is) throws IOException {
        int r = is.read();
        int g = is.read();
        int b = is.read();

        return String.format("#%02X%02X%02X", r, g, b);
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

        this.aliases.sort(Comparator.comparing(l -> l.getSortKey(this)));
    }

    public LineAlias getAlias(int id) {
        if (!idToAlias.containsKey(id)) {
            // this does actually happen sometimes due to the api not listing all line aliases
            // the original client deals with it by just ignoring the entry, I think this is at least a bit better
            System.out.println("[WARN] Vehicle with id "+id+" not found! Creating a dummy one...");

            LineAlias dummy = new LineAlias(id, String.valueOf(id), android.graphics.Color.MAGENTA, "#FF00FF", Color.WHITE, "#FFFFFF");
            idToAlias.put(id, dummy);
            nameToAlias.put(dummy.lineDisplayName(), dummy);
        }

        return idToAlias.get(id);
    }

    public LineAlias getAlias(String name) {
        LineAlias alias = nameToAlias.get(name);
        if (alias == null) return new LineAlias(0, name, android.graphics.Color.MAGENTA, "#FF00FF", Color.WHITE, "#FFFFFF");
        return alias;
    }

    public Optional<LineAlias> getOptionalAlias(String name) {
        return Optional.ofNullable(nameToAlias.get(name));
    }

    public List<LineAlias> getAllAliases() {
        return aliases;
    }
}
