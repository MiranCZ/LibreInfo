package io.github.mirancz.libreinfo.codec;

import io.github.mirancz.libreinfo.codec.debug.CompressionStats;
import io.github.mirancz.libreinfo.codec.util.AppInputStream;
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class TransitDataCodec {

    private static final long MAGIC = 0xF5444A4D0D0A1A00L;
    private static final int DATA_VERSION = 1;

    public static void write(Path path, Consumer<StreamCreator> onWrite) throws IOException {
        CompressionStats stats = new CompressionStats();

        var options = new LZMA2Options();
        try {
            options.setNiceLen(273);
        } catch (UnsupportedOptionsException e) {
            // TODO
            e.printStackTrace();
        }

        Files.createDirectories(path.getParent());

        XZOutputStream compressedOutput = new XZOutputStream(stats.track(Files.newOutputStream(path)), options);

        List<CheckedOutputStream> streams = new ArrayList<>();
        try (CheckedOutputStream output = new CheckedOutputStream(compressedOutput)) {
            StreamCreator outputProvider = s -> {
                var os = getWrappedDataOutputStream(s, output, stats, compressedOutput);

                streams.add(os);
                return os;
            };

            writeHeader(output);

            onWrite.accept(outputProvider);

            // closing writes the data, so we can't close in `finally` since at that point `output` is closed
            for (CheckedOutputStream os : streams) {
                os.close();
            }
        }
    }

    public static void readSections(Path path, Consumer<TDSection> sectionConsumer) throws IOException {
        try (var is = new AppInputStream(new XZInputStream(new BufferedInputStream(Files.newInputStream(path))))) {
            while (is.readBoolean()) {
                String name = is.readString();

                int size = is.readInt();

                // passing the whole InputStream is not ideal, but also allows some nice things
                sectionConsumer.accept(new TDSection(name, size, is));
            }
        }
    }

    private static void writeHeader(CheckedOutputStream os) throws IOException {
        os.writeLong(MAGIC);

        os.writeInt(DATA_VERSION);

        int date = Integer.parseInt(new SimpleDateFormat("yyMMddHH").format(new Date()));

        os.writeInt(date);
    }

    private static CheckedOutputStream getWrappedDataOutputStream(String s, CheckedOutputStream output, CompressionStats stats, XZOutputStream compressed) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        return new CheckedOutputStream(os) {
            boolean closed = false;

            @Override
            public void close() throws IOException {
                // we want to know since this should never happen
                if (closed) {
                    throw new IllegalStateException("Tried closing '"+s+"' stream twice! (Did a consumer close it?)");
                }
                closed = true;

                byte[] data = os.toByteArray();

                stats.beginSection(compressed);

                output.writeBoolean(true);

                output.writeString(s);

                output.writeInt(data.length);
                output.write(data);

                stats.endSection(s, data, compressed);

                super.close();
            }
        };
    }

    public interface StreamCreator {

        CheckedOutputStream create(String name);

    }

    public record TDSection(String name, int size, DataInputStream is) {
    }

}
