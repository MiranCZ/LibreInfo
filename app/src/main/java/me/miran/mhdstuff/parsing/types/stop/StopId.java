package me.miran.mhdstuff.parsing.types.stop;


import java.util.Objects;

/**
 * Helper class to work with stop IDs. Might refactor this in the future so that IDs of all data objects are mapped
 * @param internal The internal, mapped, continuous ID. This should be used for internal structures in the app.
 *                 The advantage of mapping the originals to continuous IDs is that we can work with arrays
 * @param original The original ID provided by the API/StaticData. All API calls outside of the app should use this ID
 */
public record StopId(int internal, int original) {

    public static StopId NONE = new StopId(-1, -1);


    public static StopIdHolder internal(int id) {
        return new StopIdHolder(id, StopIdType.INTERNAL);
    }

    public static StopIdHolder original(int id) {
        return new StopIdHolder(id, StopIdType.ORIGINAL);
    }

    public static final class StopIdHolder {
        public final int id;
        public final StopIdType type;

        private StopIdHolder(int id, StopIdType type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (StopIdHolder) obj;
            return this.id == that.id &&
                    Objects.equals(this.type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, type);
        }

        @Override
        public String toString() {
            return "StopIdHolder[" +
                    "id=" + id + ", " +
                    "type=" + type + ']';
        }


    }

    public enum StopIdType {
        INTERNAL, ORIGINAL
    }

}
