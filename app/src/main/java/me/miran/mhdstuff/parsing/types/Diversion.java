package me.miran.mhdstuff.parsing.types;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import me.miran.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public record Diversion(String title, DateTime from, DateTime to,
                        String publicText, List<LineAlias> lines) implements Parcelable {

    private Diversion(Parcel in) {
        this(in.readString(),
                in.readParcelable(DateTime.class.getClassLoader()),
                in.readParcelable(DateTime.class.getClassLoader()),
                in.readString(),
                in.createTypedArrayList(LineAlias.CREATOR)
        );
    }

    public static final Creator<Diversion> CREATOR = new Creator<>() {
        @Override
        public Diversion createFromParcel(Parcel in) {
            return new Diversion(in);
        }

        @Override
        public Diversion[] newArray(int size) {
            return new Diversion[size];
        }
    };

    public static List<Diversion> parseDiversions(JsonArray array, LineStorage storage) {
        return TypeHelper.parseList(array, (o) -> parse(o, storage));
    }

    public static Diversion parse(JsonObject obj, LineStorage storage) {
        if (obj == null) return null;

        String title = obj.get("title").getAsString();
        DateTime from = DateTime.parse(obj.get("from").getAsString());
        DateTime to = DateTime.parse(obj.get("to").getAsString());

        String publicText = obj.get("content").getAsString();

        List<LineAlias> lines = new ArrayList<>();

        for (JsonElement element : obj.get("lines").getAsJsonArray()) {
            lines.add(LineAlias.parse(element.getAsString(), storage));
        }

        return new Diversion(title, from, to, publicText, lines);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeParcelable(from, flags);
        dest.writeParcelable(to, flags);
        dest.writeString(publicText);
        dest.writeTypedList(lines);
    }
}
