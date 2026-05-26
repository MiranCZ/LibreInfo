package me.miran.libreinfo.util;


import me.miran.libreinfo.parsing.types.LineAlias;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data for {@link LineAlias} sometimes contain weird colors,
 * this class at least tries to fix some of those cases.
 */
public class LineColorFixer {

    private final Map<Integer, String> idToColorMap;
    private final Map<String, LineAlias> nameToAliasMap;

    public LineColorFixer(List<LineAlias> aliases) {
        idToColorMap = new HashMap<>();
        nameToAliasMap = new HashMap<>();

        for (LineAlias alias : aliases) {
            idToColorMap.put(alias.id(), alias.backgroundColorStr());
            nameToAliasMap.put(alias.lineDisplayName(), alias);
        }
    }


    public LineAlias transform(LineAlias alias) {
        String name = alias.lineDisplayName();

        // substitute connections start with 'x'
        // sometimes happens for trains that they get a different weird color
        if (name.startsWith("x")) {
            String originalName = name.substring(1);

            if (nameToAliasMap.containsKey(originalName)) {
                return alias.withBackgroundColor(nameToAliasMap.get(originalName).backgroundColorStr());
            }
        }

        // lines above ~110 have a bit different color than those up to 110, this fixed it
        // FIXME this doesn't work currently
//        if (TypeHelper.isInteger(alias.lineDisplayName()) && Integer.parseInt(alias.lineDisplayName()) > 100) {
//            for (int i = 100; i < 110; i++) {
//                if (idToColorMap.containsKey(i)) {
//                    return alias.withBackgroundColor(idToColorMap.get(i));
//                }
//            }
//        }

        return alias;
    }

}
