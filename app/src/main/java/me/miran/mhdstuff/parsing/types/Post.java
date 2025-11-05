package me.miran.mhdstuff.parsing.types;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.storage.LineStorage;
import me.miran.mhdstuff.util.IOUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @param stopID ID of the stop this post corresponds to
 * @param postID ID unique to the stop
 */
public record Post(int stopID, int postID, String name, Location location, boolean isPublic, List<LineAlias> lines) implements Parcelable {


    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            int stopId = in.readInt();
            int postId = in.readInt();

            return IdStorage.getPostStorageOrThrow().getPost(stopId, postId);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public static List<Post> parsePosts(DataInputStream array, LineStorage lineStorage) throws IOException {
        List<Post> result = new ArrayList<>();
        int size = array.readInt();

        for (int i = 0; i < size; i++) {
            result.add(parse(array, lineStorage));
        }

        return result;
    }

    public static Post parse(DataInputStream is, LineStorage lineStorage) throws IOException {
        int stopId = is.readInt();
        int postId = is.readShort();

        int nameLen = is.readInt();
        byte[] bytes = IOUtil.readNBytes(is, nameLen);

        String name = new String(bytes, StandardCharsets.UTF_8);

        double lat = is.readDouble();
        double lng = is.readDouble();

        boolean isPublic = is.readBoolean();
        List<LineAlias> lines = new ArrayList<>();

        int lineSize = is.readInt();

        for (int j = 0; j < lineSize; j++) {
            int id = is.readInt();
            lines.add(lineStorage.getAlias(id));
        }


        return new Post(stopId, postId, name, new Location(lat, lng), isPublic, lines);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(stopID);
        dest.writeInt(postID);
    }
}
