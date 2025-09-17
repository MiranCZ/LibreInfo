package com.example.mhdstuff.parsing.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public record News(int id, Time publicFrom, Time publicTo, String title, String text, String link) {

    public static List<News> parseNewsList(JsonArray array) {
        return TypeHelper.parseList(array, News::parse);
    }

    public static News parse(JsonObject obj) {
        if (obj == null) return null;

        int id = obj.get("Number").getAsInt();
        Time publicFrom = Time.parse(obj.get("PublicFrom").getAsString());
        Time publicTo = Time.parse(obj.get("PublicTo").getAsString());
        String title = obj.get("Title").getAsString();
        String text = obj.get("Text").getAsString();
        String link = obj.get("Link").getAsString();

        // fuck their formatting bro
        if (text.endsWith(" ...")) {
            text = text.substring(0, text.length()-4)+"...";
        }

        return new News(id, publicFrom, publicTo, title, text, link);
    }
}
