package me.miran.libreinfo.parsing.types;

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
        if (stopMode.isEmpty()) return StopMode.ALL;

        if (stopMode.equals("z")) return StopMode.Z;
        if (stopMode.equals("x")) return StopMode.X;
        if (stopMode.equals("w")) return StopMode.W;
        if (stopMode.equals("o")) return StopMode.O;
        if (stopMode.equals("*")) return StopMode.MIXED;

        System.out.println("[WARN] Unknown stop mode " + stopMode);
        return StopMode.MIXED;
    }
}
