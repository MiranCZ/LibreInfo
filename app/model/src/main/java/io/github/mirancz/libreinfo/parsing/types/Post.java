package io.github.mirancz.libreinfo.parsing.types;

import io.github.mirancz.libreinfo.parsing.types.stop.Stop;

/**
 * @param stop The stop this post corresponds to
 * @param postID ID unique to the stop
 */
public record Post(Stop stop, int postID, String name, Location location) {

}
