package io.github.mirancz.gtfs.export;

import io.github.mirancz.gtfs.ParsedGtfs;
import io.github.mirancz.util.CheckedOutputStream;

import java.util.function.Function;

public interface Writer {

    void write(ParsedGtfs gtfs, Function<String, CheckedOutputStream> outputProvider);

}
