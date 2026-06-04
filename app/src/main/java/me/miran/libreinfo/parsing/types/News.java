package me.miran.libreinfo.parsing.types;

import androidx.core.text.HtmlCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public record News(DateTime date, String title, String htmlContent, String link) {

    public static List<News> parseNewsList(JsonArray array) {
        return TypeHelper.parseList(array, News::parse);
    }

    public static News parse(JsonObject obj) {
        if (obj == null) return null;

        DateTime date = DateTime.parseEpoch(obj.get("date").getAsString());
        String title = obj.get("title").getAsString();
        String text = obj.get("content_html").getAsString();
        String link = obj.get("url").getAsString();

        return new News(date, title, text, link);
    }
    
    public String getPlaintext() {
        return HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
    }
    
}
