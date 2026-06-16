package me.miran.libreinfo.parsing.types;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public record News(DateTime date, String title, String htmlContent, String link) implements Parcelable {

    private News(Parcel in) {
        this(in.readParcelable(DateTime.class.getClassLoader()), in.readString(), in.readString(), in.readString());
    }

    public static final Creator<News> CREATOR = new Creator<>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(date, flags);
        dest.writeString(title);
        dest.writeString(htmlContent);
        dest.writeString(link);
    }
}
