package io.github.mirancz.gtfs.export.writer;

import io.github.mirancz.gtfs.ParsedGtfs;
import io.github.mirancz.gtfs.export.SingleWriter;
import io.github.mirancz.gtfs.ids.ApiEntry;
import io.github.mirancz.gtfs.remap.IdType;
import io.github.mirancz.gtfs.remap.Mapper;
import io.github.mirancz.util.CheckedOutputStream;

import java.io.IOException;

public class ApiWriter extends SingleWriter {

    public ApiWriter() {
        super("api");
    }

    @Override
    public void write(ParsedGtfs gtfs, CheckedOutputStream os) throws IOException {
        var api = gtfs.api();
        os.writeInt(api.size());

        for (ApiEntry e : api) {
            os.writeInt(Mapper.map(IdType.TRIP, e.trip_id()));
        }
        for (ApiEntry e : api) os.writeShort(e.line_id());
        for (ApiEntry e : api) os.writeShort(e.route_id());
//
//        for (ApiEntry e : api) {
//            os.writeInt(e.trip_id());
//
//            os.writeShort(e.line_id());
//            os.writeShort(e.route_id());
//        }
    }

}
