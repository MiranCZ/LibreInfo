package me.miran.libreinfo.parsing.types;

import me.miran.libreinfo.util.AppLog;

import java.util.List;

public record MinuteRange(int from, int to) {

    public static final MinuteRange NONE = new MinuteRange(-1, -1);


    public static MinuteRange parse(String str) {
        if (str == null) return null;
        str = str.strip();

        if (str.isEmpty()) return NONE;


        @SuppressWarnings("UnnecessaryUnicodeEscape") // Unicode so its apparent these are not two minus signs
        List<Character> separators = List.of('-', '\u2013');

        // these mfs use multiple different symbols for range...
        for (char separator : separators) {
            if (!str.contains(String.valueOf(separator))) continue;

            String[] parts = str.split(String.valueOf(separator));
            return new MinuteRange(Integer.parseInt(parts[0].strip()), Integer.parseInt(parts[1].strip()));
        }

        AppLog.w("Another separator that isn't handled for ranges! "+str);

        // try to recover at least somehow
        String from = "";
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            from += ch;

            if (!TypeHelper.isInteger(from)) {
                from = from.substring(0, from.length()-1);
                break;
            }
        }

        String to = "";
        for (int i = str.length() - 1; i >= 0; i--) {
            char ch = str.charAt(i);
            to = ch + to;

            if (!TypeHelper.isInteger(to)) {
                to = to.substring(1);
                break;
            }
        }

        return new MinuteRange(Integer.parseInt(from), Integer.parseInt(to));
    }

    public String format() {
        return from + " - "+to;
    }
}
