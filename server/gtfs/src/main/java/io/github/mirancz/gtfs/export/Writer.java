package io.github.mirancz.gtfs.export;

import io.github.mirancz.gtfs.ParsedGtfs;
import io.github.mirancz.libreinfo.codec.TransitDataCodec;
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream;

import java.util.function.Function;

public interface Writer {

    void write(ParsedGtfs gtfs, TransitDataCodec.StreamCreator creator);

}
