package io.github.mirancz.gtfs.export;

import io.github.mirancz.gtfs.ParsedGtfs;
import io.github.mirancz.util.CheckedOutputStream;

import java.io.IOException;
import java.util.function.Function;

public abstract class SingleWriter implements Writer {

    private final String name;

    public SingleWriter(String name) {
        this.name = name;
    }

    @Override
    public void write(ParsedGtfs gtfs, Function<String, CheckedOutputStream> outputProvider) {
        try (var os = outputProvider.apply(name)) {
            this.write(gtfs, os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void write(ParsedGtfs gtfs, CheckedOutputStream os) throws IOException;


}
