package io.github.mirancz.debug;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.*;

/**
 * Logs the compressed size of every section of the data file.
 * <p>
 * "in stream" is what the section adds to the written file, where it can reuse data from earlier
 * sections; "standalone" is its size when compressed on its own, as it would be in a separate file.
 */
public final class CompressionStats {

    private CountingOutputStream counter;
    private long rawTotal;
    private long mark;

    /** Counts what the compressor writes to {@code out}; the compressor has to be put on the returned stream. */
    public OutputStream track(OutputStream out) {
        counter = new CountingOutputStream(out);
        return counter;
    }

    // flushing ends the current LZMA2 chunk, so everything written so far has reached the counter
    public void beginSection(Flushable compressed) throws IOException {
        compressed.flush();
        mark = counter.count;
    }

    public void endSection(String name, byte[] data, Flushable compressed) throws IOException {
        compressed.flush();
        rawTotal += data.length;

        System.out.printf("[STATS] %-16s %,12d B raw %,12d B in stream %,12d B standalone%n",
                name, data.length, counter.count - mark, standaloneSize(data));
    }

    /** Call once the compressed stream is closed, so the total includes the XZ footer. */
    public void printTotal() {
        System.out.printf("[STATS] %-16s %,12d B raw %,12d B file%n", "total", rawTotal, counter.count);
    }

    private static long standaloneSize(byte[] data) throws IOException {
        CountingOutputStream counter = new CountingOutputStream(OutputStream.nullOutputStream());

        try (XZOutputStream xz = new XZOutputStream(counter, new LZMA2Options())) {
            xz.write(data);
        }

        return counter.count;
    }

    private static final class CountingOutputStream extends FilterOutputStream {

        private long count;

        private CountingOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            count++;
        }

        // FilterOutputStream's default writes byte by byte
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            count += len;
        }
    }
}
