package io.github.mirancz.libreinfo.parsing.types;

public class TypeHelper {

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException ingored) {
            return false;
        }

        return true;
    }

}
