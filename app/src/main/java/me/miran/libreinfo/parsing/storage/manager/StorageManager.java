package me.miran.libreinfo.parsing.storage.manager;

import static me.miran.libreinfo.util.StorageFile.*;

import android.content.Context;
import android.os.Looper;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import me.miran.libreinfo.R;
import me.miran.libreinfo.exception.AppException;
import me.miran.libreinfo.exception.ErrorType;
import me.miran.libreinfo.exception.StorageInitException;
import me.miran.libreinfo.parsing.storage.AppStorage;
import me.miran.libreinfo.util.AppInputStream;
import me.miran.libreinfo.util.AppLog;
import me.miran.libreinfo.util.StorageFile;
import me.miran.libreinfo.util.request.RequestHelper;

import org.tukaani.xz.XZInputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class StorageManager {

    private long serverUpdateTime = -1;
    private static boolean initCalled = false;
    private final Context context;


    public StorageManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void init() throws AppException {
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new RuntimeException("Should be called from background thread since blocking operations are performed");
        }
        if (initCalled) {
            throw new IllegalStateException("'init' called multiple times!");
        }
        initCalled = true;

        try {
            serverUpdateTime = RequestHelper.getLastStaticUpdate(context);
        } catch (AppException e) {
            AppLog.w("failed to get static update timestamp, using cached data", e);
        }

        DataCachedState state = getDataState();

        if (state.isPresent()) {
            if (!state.isExtracted()) {
                try {
                    inflateData(true);
                } catch (AppException e) {
                    AppLog.e("Failed to inflate data", e);

                    fullDataReload(true);
                    return;
                }
            }

            try {
                loadData(true);
            } catch (AppException e) {
                AppLog.e("Failed to load data", e);

                fullDataReload(true);
                return;
            }

            if (state.isStale()) {
                // TODO extract and try to parse in a different folder
                fullDataReload(false);
            }
        } else {
            fullDataReload(false);
        }
    }

    private void fullDataReload(boolean invalidateCache) throws AppException {
        if (invalidateCache) invalidateCache();

        downloadData();
        inflateData(false);
        loadData(false);
    }

    private void downloadData() throws AppException {
        try (var data = RequestHelper.getData(context)) {
            writeToCache(data, "data");
        } catch (IOException e) {
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
        }
    }

    private void loadData(boolean update) throws AppException {
        Function1<AppStorage, Unit> consumer = null;

        if (update) {
            consumer = (ins) -> {
                AppContainer.INSTANCE.getStorageProvider().update(ins);
                return Unit.INSTANCE;
            };
        }

        try {
            IdStorage instance = new StorageBuilder(context, consumer).build(this);
            AppContainer.INSTANCE.getStorageProvider().update(instance);
        } catch (StorageInitException e) {
            throw e.appException();
        }
    }

    private void invalidateCache() throws AppException {
        try {
            Files.deleteIfExists(getCachedPath("data"));

            for (var file : StorageFile.values()) {
                Files.deleteIfExists(getCachedPath(file));
            }
        } catch (IOException e) {
            throw new AppException("Failed to delete cache (uh oh)", e);
        }
    }

    private DataCachedState getDataState() {
        Path metaPath = getCachedMetaPath( "data");
        if (!getCachedPath("data").toFile().exists() || !metaPath.toFile().exists()) {
            return DataCachedState.MISSING;
        }

        boolean present = true;
        boolean stale = false;

        // if we don't have update time just don't mark as stale
        if (serverUpdateTime != -1) {
            try {
                byte[] timeB = Files.readAllBytes(metaPath);
                long time = bytesToLong(timeB);

                if (time < serverUpdateTime) {
                    stale = true;
                    AppLog.d("found old cache file for 'data'");
                }
            } catch (IOException e) {
                AppLog.e("failed to read data.meta file", e);
                return DataCachedState.MISSING;
            }
        }

        boolean allExtraced = true;

        for (var file : StorageFile.values()) {
            if (!Files.exists(getCachedPath(file))) {
                AppLog.d(file.fileName + " was not previously extracted, falling back");
                allExtraced = false;
                break;
            }
        }

        return new DataCachedState(present, stale, allExtraced);

    }

    private void inflateData(boolean skipExisting) throws AppException {
        try (var is = new AppInputStream(new XZInputStream(new BufferedInputStream(new FileInputStream(getCachedPath("data").toFile()))))) {
            while (is.readBoolean()) {
                String name = is.readString();
                String tmpName = name + ".tmp";

                Path tmpPath = getCachedPath(tmpName);
                Path finalPath = getCachedPath(name);

                AppLog.d("extracting " + name);
                int dataLen = is.readInt();

                if (Files.exists(tmpPath)) {
                    Files.delete(tmpPath);
                    AppLog.d("deleting stale temp entry for " + name);
                } else if (Files.exists(finalPath) && skipExisting) {
                    AppLog.d(name + " already exists, skipping");

                    long skipped = 0;

                    while (skipped != dataLen) {
                        if (skipped > dataLen) {
                            throw new IOException("Skiped more than " + dataLen + " bytes (" + skipped + ")");
                        }

                        long newSkipped = is.skip(dataLen - skipped);
                        if (newSkipped == 0) {
                            throw new IOException("Failed to skip " + dataLen + " bytes (" + skipped + ")");
                        }

                        skipped += newSkipped;
                    }
                    continue;
                }

                byte[] buffer = new byte[8192];

                int len;

                try (FileOutputStream fos = new FileOutputStream(tmpPath.toFile())) {
                    while ((len = is.read(buffer, 0, Math.min(dataLen, buffer.length))) != -1) {
                        dataLen -= len;

                        fos.write(buffer, 0, len);
                        if (dataLen <= 0) break;
                    }

                    if (dataLen > 0) {
                        throw new IOException("Unexpected EOF extracting " + name + ", " + dataLen + " bytes short");
                    }
                }

                // rename to normal name only after fully written
                // (to mitigate half-written files when app is closed etc.)
                Files.move(tmpPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
        }
    }

    public ByteBuffer getRouteStopsBuff() throws AppException {
        try(var channel = FileChannel.open(getCachedPath(ROUTE_STOPS), StandardOpenOption.READ)) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        } catch (IOException e) {
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
        }
    }

    public <E> E useApi(DataUser<AppInputStream, E> dataUser) throws AppException {
        return useCache(dataUser, API);
    }

    public <E> E useStopMapping(DataUser<AppInputStream, E> dataUser) throws AppException {
        return useCache(dataUser, STOP_MAPPING);
    }

    public <E> E useCalendar(DataUser<AppInputStream, E> dataUser) throws AppException {
        return useCache(dataUser, CALENDAR);
    }

    public <E> E useCalendarDates(DataUser<AppInputStream, E> dataUser) throws AppException {
        return useCache(dataUser, CALENDAR_DATES);
    }

    public <E> E useStopTimes(DataUser<AppInputStream, E> dataUser) throws AppException {
        return useCache(dataUser, STOP_TIMES);
    }

    public <E> E useTrips(DataUser<AppInputStream, E> dataUser) throws AppException {
        return useCache(dataUser, TRIPS);
    }

    public <E> E useStops(DataUser<AppInputStream, E> dataUser) throws AppException {
        return useCache(dataUser, STOPS);
    }

    public <E> E useLineAliases(DataUser<AppInputStream, E> dataUser) throws AppException {
        return useCache(dataUser, LINE_ALIASES);
    }

    public <E> E usePosts(DataUser<AppInputStream, E> dataUser) throws AppException {
        return useCache(dataUser, POSTS);
    }


    private <E> E useCache(DataUser<AppInputStream, E> dataUser, StorageFile file) throws AppException {
        return useCache(dataUser, file.fileName);
    }

    private <E> E useCache(DataUser<AppInputStream, E> dataUser, String... name) throws AppException {
        try (AppInputStream stream = new AppInputStream(new BufferedInputStream(new FileInputStream(getCachedPath(name).toFile())))) {
            return dataUser.use(stream);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
        }
    }

    @FunctionalInterface
    public interface DataUser<T, E> {
        E use(T t) throws AppException;
    }

    public void writeToCache(InputStream is, String... name) throws AppException {
        Path path = getCachedPath(name);
        Path metaPath = getCachedMetaPath(name);

        var buffer = new byte[8192];
        try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            int n;
            while ((n = is.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new AppException("Failed to write cache", e);
        }

        try {
            Files.write(metaPath, getCacheTime(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new AppException("Failed to write cache meta", e);
        }
    }

    private byte[] getCacheTime() {
        long current;

        if (serverUpdateTime != -1) {
            current = serverUpdateTime;
        } else {
            // safety fallback
            current = Long.MIN_VALUE;
        }

        return ByteBuffer.allocate(Long.BYTES).putLong(current).array();
    }

    private long bytesToLong(byte[] bytes) {
        if (bytes.length != 8)
            throw new IllegalArgumentException("Byte array must be 8 bytes long.");

        return ByteBuffer.wrap(bytes).getLong();
    }

    private Path getCachedPath(StorageFile file) {
        return getCachedPath(file.fileName);
    }

    private Path getCachedPath(String... name) {
        Path path = context.getFilesDir().toPath();

        for (String s : name) {
            path = path.resolve(s);
        }

        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            AppLog.e("Failed to create directories", e);
        }

        return path;
    }

    private Path getCachedMetaPath(String... name) {
        return getCachedPath(name).getParent().resolve(name[name.length - 1] + ".meta");
    }

    record DataCachedState(boolean present, boolean stale, boolean extracted) {

        public static final DataCachedState MISSING = new DataCachedState(false, false, false);

        public boolean isMissing() {
            return !present;
        }

        public boolean isPresent() {
            return present;
        }

        public boolean isExtracted() {
            return extracted;
        }

        public boolean isStale() {
            return stale;
        }

    }

}
