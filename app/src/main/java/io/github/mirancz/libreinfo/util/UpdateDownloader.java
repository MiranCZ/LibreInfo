package io.github.mirancz.libreinfo.util;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.github.mirancz.libreinfo.BuildConfig;
import io.github.mirancz.libreinfo.exception.AppException;
import io.github.mirancz.libreinfo.exception.RequestException;
import io.github.mirancz.libreinfo.util.request.RequestHelper;


/**
 * Checks the GitHub releases for a potential new release and downloads it if available
 */
// TODO better GitHub requesting (ETag, handling ratelimitting/error states better)
public class UpdateDownloader {

    public enum UpdateResult {
        UP_TO_DATE, DOWNLOADED, FAILURE, ERROR
    }

    public static UpdateResult checkAndDownload(Context context) {
        try {
            JsonObject releaseInfo = RequestHelper.getLatestReleaseInfo(context);

            String versionUrl = findAssetDownload(releaseInfo, "version.json");
            JsonObject versionInfo = RequestHelper.readJsonUrl(context, versionUrl, "Release version meta", JsonObject.class);

            int releaseVersionCode = versionInfo.get("versionCode").getAsInt();

            // we should update
            if (BuildConfig.VERSION_CODE < releaseVersionCode) {
                return downloadApk(context, releaseInfo, versionInfo);
            }

            return UpdateResult.UP_TO_DATE;
        } catch (AppException e) {
            AppLog.w("App update failed ", e);
            return UpdateResult.FAILURE;
        } catch (Exception e) {
            AppLog.e("An unexpected error occurred", e);
            return UpdateResult.ERROR;
        }
    }

    private static UpdateResult downloadApk(Context context, JsonObject releaseInfo, JsonObject versionInfo) {
        JsonObject apksInfo = versionInfo.getAsJsonObject("apks");

        String buildAbi = getBuildABI();

        JsonObject apkEntry;
        if (apksInfo.has(buildAbi)) {
            apkEntry = apksInfo.getAsJsonObject(buildAbi);
        } else {
            apkEntry = apksInfo.getAsJsonObject("universal");
        }

        if (apkEntry == null) {
            AppLog.e("Release version meta has no apk entry for this device");
            return UpdateResult.ERROR;
        }

        String fetchApkName = apkEntry.get("name").getAsString();
        String expectedHash = apkEntry.get("hash").getAsString();

        String downloadUrl;
        try {
            downloadUrl = findAssetDownload(releaseInfo, fetchApkName);
        } catch (FileNotFoundException e) {
            AppLog.e("Failed to find apk",e);
            return UpdateResult.ERROR;
        }

        Path apkFile = getUpdateFilePath(context);
        Path metaFile = getUpdateMetaPath(context);
        Path apkPart = apkFile.resolveSibling(apkFile.getFileName() + ".part");
        Path metaPart = metaFile.resolveSibling(metaFile.getFileName() + ".part");

        try (var is = RequestHelper.readUrl(context, downloadUrl, "Apk download")) {
            Files.createDirectories(getUpdatesDir(context));

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (var os = new FileOutputStream(apkPart.toFile())) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;

                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                    os.write(buffer, 0, bytesRead);
                }
            }

            String actualHash = toHex(digest.digest());
            if (!actualHash.equalsIgnoreCase(expectedHash)) {
                AppLog.w("Downloaded apk hash mismatch for " + fetchApkName + " (expected " + expectedHash + ", got " + actualHash + ")");

                return UpdateResult.FAILURE;
            }



            var meta = new JsonObject();
            meta.addProperty("versionCode", versionInfo.get("versionCode").getAsInt());
            meta.addProperty("hash", actualHash);

            //noinspection ReadWriteStringCanBeUsed - can't because of older android versions
            Files.write(metaPart, meta.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // whole file is written and verified
            Files.move(apkPart, apkFile, StandardCopyOption.REPLACE_EXISTING);
            Files.move(metaPart, metaFile, StandardCopyOption.REPLACE_EXISTING);

            return UpdateResult.DOWNLOADED;
        } catch (NoSuchAlgorithmException e) {
            AppLog.e("SHA-256 algorithm unavailable", e);
            return UpdateResult.ERROR;
        } catch (IOException | RequestException e) {
            AppLog.w("IO failed", e);
            return UpdateResult.FAILURE;
        } finally {
            deleteQuietly(apkPart);
            deleteQuietly(metaPart);
        }
    }

    @NonNull
    private static String findAssetDownload(JsonObject releaseInfo, String assetName) throws FileNotFoundException {
        for (JsonElement assetEl : releaseInfo.getAsJsonArray("assets")) {
            var asset = assetEl.getAsJsonObject();

            if (asset.get("name").getAsString().equals(assetName)) {
                return asset.get("browser_download_url").getAsString();
            }
        }

        throw new FileNotFoundException("Failed to find fitting asset in release for '"+ assetName +"'");
    }

    /** Lowercase hex encoding, matching the format published in {@code version.json}. */
    @NonNull
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    public static Path getUpdateFilePath(Context context) {
        return getUpdatesDir(context).resolve("update_file.apk");
    }

    public static Path getUpdateMetaPath(Context context) {
        return getUpdatesDir(context).resolve("meta.json");
    }

    public static Path getUpdatesDir(Context context) {
        return context.getNoBackupFilesDir().toPath().resolve("update");
    }

    private static String getBuildABI() {
        var supported = Build.SUPPORTED_ABIS;

        if (supported.length > 0) {
            return supported[0];
        }

        return "universal";
    }

}
