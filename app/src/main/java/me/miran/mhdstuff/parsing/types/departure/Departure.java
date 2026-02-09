package me.miran.mhdstuff.parsing.types.departure;

import java.util.List;

public record Departure(int postID, String name, List<DepartureEntry> entries) {

}
