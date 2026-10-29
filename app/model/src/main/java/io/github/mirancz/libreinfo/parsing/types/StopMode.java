package io.github.mirancz.libreinfo.parsing.types;

public enum StopMode {
    // FIXME there is also a "t" mode??

    ALL, // stops always 
    Z, // signalled always
    O, // signalled between 20:00 - 5:00 (8PM - 5AM)
    W, // signalled between 20:00 - 5:00 (8PM - 5AM) and on nonwork days
    X, // signalled between 19:00 - 6:00 (7PM - 6AM) and on nonwork days
    MIXED // multiple vehicles, every has a different mode
    ;

    public static StopMode parse(String stopMode) {
        stopMode = stopMode.strip().toLowerCase();
        return switch (stopMode) {
            case "" -> StopMode.ALL;
            case "z" -> StopMode.Z;
            case "x" -> StopMode.X;
            case "w" -> StopMode.W;
            case "o" -> StopMode.O;
            case "*" -> StopMode.MIXED;
            default -> StopMode.MIXED;
        };

    }
}
