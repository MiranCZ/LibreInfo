package io.github.mirancz.libreinfo.parsing.types.departure;

import java.util.List;
import java.util.stream.Collectors;

public record Departures(String message, List<Departure> departures) {

}
