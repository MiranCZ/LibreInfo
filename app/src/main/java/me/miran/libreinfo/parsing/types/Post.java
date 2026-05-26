package me.miran.libreinfo.parsing.types;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import me.miran.libreinfo.parsing.storage.IdStorage;
import me.miran.libreinfo.parsing.storage.StopStorage;
import me.miran.libreinfo.parsing.types.stop.Stop;
import me.miran.libreinfo.parsing.types.stop.StopId;
import me.miran.libreinfo.util.IOUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @param stop The stop this post corresponds to
 * @param postID ID unique to the stop
 */
public record Post(Stop stop, int postID, String name, Location location) implements Parcelable {


    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            int stopId = in.readInt();
            int postId = in.readInt();

            return IdStorage.getPostStorageOrBlock().getPost(stopId, postId);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public static List<Post> parsePosts(DataInputStream array, StopStorage stopStorage) throws IOException {
        List<Post> result = new ArrayList<>();
        int size = array.readInt();

        for (int i = 0; i < size; i++) {
            result.add(parse(array, stopStorage));
        }

        return result;
    }

    public static Post parse(DataInputStream is, StopStorage stopStorage) throws IOException {
        int stopId = is.readShort();
        int postId = is.readShort();

        int nameLen = is.readInt();
        byte[] bytes = IOUtil.readNBytes(is, nameLen);

        String name = new String(bytes, StandardCharsets.UTF_8);

        double lat = is.readDouble();
        double lng = is.readDouble();

        Stop stop = stopStorage.getStop(StopId.internal(stopId));

        return new Post(stop, postId, name, new Location(lat, lng));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(stop.id.internal());
        dest.writeInt(postID);
    }
}
