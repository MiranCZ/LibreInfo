package io.github.mirancz.libreinfo.parsing.types;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record LineAlias(int id, String lineDisplayName, int backgroundColor, String backgroundColorStr, int textColor, String textColorStr) implements Comparable<LineAlias> {

    private static String normalizeColor(String colorStr) {
        String saved = colorStr;
        colorStr = colorStr.strip();
        if (colorStr.isEmpty()) {
            return "#000000";
        }

        if (colorStr.startsWith("#")) {
            colorStr = colorStr.substring(1);
        }
        String temp = colorStr;
        while (colorStr.length() < 6) {
            colorStr = colorStr + temp;
        }
        if (colorStr.length() != 6) {
            throw new IllegalStateException("What even is this input??? "+saved);
        }

        return "#"+colorStr;
    }

    public LineAlias withBackgroundColor(String colorStr) {
        int color = parseColor(colorStr);

        return new LineAlias(id, lineDisplayName, color, colorStr, textColor, textColorStr);
    }

    // from android.graphics.Colro#parseColor
    public static int parseColor(String colorString) {
        if (colorString.charAt(0) != '#') throw new IllegalArgumentException("Unknown color");

        // Use a long to avoid rollovers on #ffXXXXXX
        long color = Long.parseLong(colorString.substring(1), 16);

        if (colorString.length() == 7) {
            // Set the alpha value
            color |= 0x00000000ff000000;
        } else if (colorString.length() != 9) {
            throw new IllegalArgumentException("Unknown color");
        }
        return (int) color;
    }

    public int getSortKey(Function<String, Optional<LineAlias>> aliasMapper) {
        int base = id*10;

        if (lineDisplayName.equals(String.valueOf(id))) {
            return base;
        }

        List<Character> prefixes = List.of('x','E', 'P', 'H');

        for (int i = 0; i < prefixes.size(); i++) {
            Character ch = prefixes.get(i);

            if (lineDisplayName.startsWith(ch + "")) {
                String subStr = lineDisplayName.substring(1);

                Optional<LineAlias> other = aliasMapper.apply(subStr);
                if (other.isPresent()) {
                    return other.get().getSortKey(aliasMapper)+i+1;
                }

                if (TypeHelper.isInteger(subStr)) {
                    return Integer.parseInt(subStr)*10;
                }
                return base;
            }
        }


        return base;
    }

    // TODO this should probably be used in all usages of some line lists
    //  (rather than trusting the API to sort them)
    @Override
    public int compareTo(LineAlias o) {
        // TODO compare info could be cached
        return getCompareInfo().compareTo(o.getCompareInfo());
    }

    /**
     * Tries to generate at least somewhat sensible sorting rules for the lines from their names rather than IDs
     */
    private CompareInfo getCompareInfo() {
        String name = lineDisplayName.toLowerCase();

        if (name.equals("x")) {
            return new CompareInfo(19, LineComparePower.UNKNOWN);
        }
        if (name.equals("loď")) {
            return new CompareInfo(100, LineComparePower.UNKNOWN);
        }

        LineComparePower power = LineComparePower.UNKNOWN;

        // power pass
        if (name.startsWith("x")) {
            power = LineComparePower.SUBSTITUTE;
            name = name.substring(1);
        } else if (name.startsWith("p")) {
            power = LineComparePower.EXTRA;
            name = name.substring(1);
        } else if (name.startsWith("e")) {
            power = LineComparePower.EXPRESS;
            name = name.substring(1);
        } else if (TypeHelper.isInteger(lineDisplayName)) {
            power = LineComparePower.PRIMARY;
        }

        // offset pass
        int offset = 0;

        boolean resolved = false;
        // night lines
        if (name.startsWith("n")) {
            name = name.substring(1);
            resolved = true;
        }
        // schools
        else if (name.startsWith("š")) {
            name = name.substring(1);
            resolved = true;
        }
        // trains
        else if (name.startsWith("r") || name.startsWith("s")) {
            offset = 10_000;

            name = name.substring(1);
            resolved = true;
        }

        if (resolved && power == LineComparePower.UNKNOWN) {
            power = LineComparePower.PRIMARY;
        }

        if (TypeHelper.isInteger(name)) {
            return new CompareInfo(Integer.parseInt(name)+offset, power);
        }

        // safety fallback where removing the first letter makes a valid line number
        if (TypeHelper.isInteger(name.substring(1))) {
            power = LineComparePower.UNKNOWN;
            name = name.substring(1);

            return new CompareInfo(Integer.parseInt(name)+offset, power);
        }

        // TODO logging
//        AppLog.w("Failed to generate comparison for '"+lineDisplayName+"'");
        return new CompareInfo(id+offset, LineComparePower.UNKNOWN);
    }

    private record CompareInfo(int number, LineComparePower power) implements Comparable<CompareInfo> {
        @Override
        public int compareTo(CompareInfo o) {
            if (number == o.number) {
                return power.compareTo(o.power);
            }

            return Integer.compare(number, o.number);
        }
    }

    private enum LineComparePower implements Comparable<LineComparePower> {
        /// normal lines
        PRIMARY,
        ///  express lines (eq. E75)
        EXPRESS,
        /// lines used to help with diversions (eq. x10)
        SUBSTITUTE,
        /// lines used for helping with traffic (eq. P1)
        EXTRA,
        ///  other or unknown
        UNKNOWN;

    }

}
