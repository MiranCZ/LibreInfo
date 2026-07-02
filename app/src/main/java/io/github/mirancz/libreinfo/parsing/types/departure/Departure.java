package io.github.mirancz.libreinfo.parsing.types.departure;

import java.util.List;

public record Departure(int postID, String name, List<DepartureEntry> entries) {

}
