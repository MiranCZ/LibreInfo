package me.miran.mhdstuff.parsing.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TypeHelper {

    public static <T> List<T> parseList(JsonArray array, Function<JsonObject, T> parseFunction) {
        if (array == null) return List.of();

        List<T> result = new ArrayList<>();

        for (JsonElement element : array) {
            result.add(parseFunction.apply(element.getAsJsonObject()));
        }

        return result;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException ingored) {
            return false;
        }

        return true;
    }

}
