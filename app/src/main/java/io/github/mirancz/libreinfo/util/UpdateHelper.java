package io.github.mirancz.libreinfo.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.github.mirancz.libreinfo.BuildConfig;

public class UpdateHelper {

    public static boolean isUpdateDownloaded(Context context) {
        var path = UpdateDownloader.getUpdateMetaPath(context);

        if (!Files.exists(path)) return false;

        try (var reader = new FileReader(path.toFile())) {
            var json = new Gson().fromJson(reader, JsonObject.class);

            if (json.has("versionCode")) {
                var versionCode = json.get("versionCode").getAsInt();
                var hash = json.get("hash").getAsString();

                if (!hasValidApkFile(context, hash)) return false;

                return versionCode > BuildConfig.VERSION_CODE;
            }
        } catch (IOException e) {
            AppLog.e("Failed to read update metadata file", e);
            return false;
        } catch (JsonIOException | JsonSyntaxException e) {
            AppLog.e("Failed to parse json", e);
            return false;
        }

        return false;
    }

    private static boolean hasValidApkFile(Context context, String expectedHash) {
        var path = UpdateDownloader.getUpdateFilePath(context);
        if (!Files.exists(path)) return false;

        try (var is = new FileInputStream(path.toFile())) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            if (UpdateDownloader.toHex(digest.digest()).equals(expectedHash)) {
                return true;
            }

            AppLog.d("Stashed apk hash does not match");

            return false;
        } catch (IOException e) {
            AppLog.e("Failed to read apk file", e);
            return false;
        } catch (NoSuchAlgorithmException e) {
            AppLog.e("SHA-256 algorithm unavailable", e);

            return false;
        }
    }

}
