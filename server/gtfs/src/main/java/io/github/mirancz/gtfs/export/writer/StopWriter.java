package io.github.mirancz.gtfs.export.writer;

import io.github.mirancz.gtfs.ParsedGtfs;
import io.github.mirancz.gtfs.export.SingleWriter;
import io.github.mirancz.gtfs.export.StopInfo;
import io.github.mirancz.gtfs.ids.StopEntry;
import io.github.mirancz.util.CheckedOutputStream;

import java.io.IOException;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.mirancz.gtfs.export.ParseUtil.parseStop;

public class StopWriter extends SingleWriter {

    public StopWriter() {
        super("stops");
    }

    @Override
    protected void write(ParsedGtfs gtfs, CheckedOutputStream os) throws IOException {
        var stops = gtfs.stops();

        // gtfs stops file contains same stops but with different platforms (which we don't care about)

        stops.removeIf(new Predicate<>() {
            final HashSet<Integer> processed = new HashSet<>();

            @Override
            public boolean test(StopEntry e) {
                int stopId = parseStop(e.stop_id()).stopId();

                if (processed.contains(stopId)) return true;
                processed.add(stopId);

                return false;
            }
        });

        os.writeInt(stops.size());

        for (StopEntry e : stops) {
            int stopId = parseStop(e.stop_id()).stopId();

            os.writeShort(stopId);

            StopInfo parent = parseStop(e.parent_station());

            short packedPost = parent.postId();
            if (parent.type() == StopInfo.PostType.PLATFORM) {
                packedPost |= (short) (1<<15);
            }

            if (parent.stopId() != stopId) {
                throw new IllegalStateException("Parent stop id does not match stop id of child! "+stopId + " vs "+parent.stopId() + " for "+e);
            }
            os.writeShort(packedPost);

            os.writeLocation(e.stop_lat(), e.stop_lon());
        }

        for (StopEntry e : stops) {
            os.writeString(e.stop_name());
//            os.writeString(e.parent_station());
        }
    }


}
