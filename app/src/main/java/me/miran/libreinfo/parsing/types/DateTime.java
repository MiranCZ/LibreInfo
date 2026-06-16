package me.miran.libreinfo.parsing.types;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public record DateTime(int day, int month, int year, int hours, int minutes) implements Parcelable {

    public static final DateTime NONE = new DateTime(-1, -1, -1, -1, -1);


    private DateTime(Parcel in) {
        this(in.readInt(), in.readInt(),in.readInt(),in.readInt(),in.readInt());
    }

    public static final Creator<DateTime> CREATOR = new Creator<>() {
        @Override
        public DateTime createFromParcel(Parcel in) {
            if (in.readByte() == 0) return NONE;

            return new DateTime(in);
        }

        @Override
        public DateTime[] newArray(int size) {
            return new DateTime[size];
        }
    };

    public static DateTime now() {
        LocalDateTime now = LocalDateTime.now();

        return new DateTime(now.getDayOfMonth(), now.getMonthValue(), now.getYear(), now.getHour(), now.getMinute());
    }

    /**
     * The format seems to be {@code DD.MM.YYYY HH:mm}
     */
    public static DateTime parse(String str) {
        if (str == null) return null;

        // sometimes time can be undefined in arbitrary ways :)
        if (str.isBlank() || str.equals("?")) {
            return NONE;
        }

        String time = str.substring(str.length()-5);
        String[] partsTime = time.strip().split(":");
        int hours = Integer.parseInt(partsTime[0].strip());
        int minutes = Integer.parseInt(partsTime[1].strip());

        str = str.substring(0, str.length()-6);
        String[] partsDate = str.strip().split("\\.");

        int day = Integer.parseInt(partsDate[0].strip());
        int month = Integer.parseInt(partsDate[1].strip());
        int year = Integer.parseInt(partsDate[2].strip());

        return new DateTime(day, month, year, hours, minutes);
    }

    public static DateTime parseISO8601(String str) {
        var timestamp = Instant.parse(str);

        ZonedDateTime zdt = timestamp.atZone(ZoneId.of("Europe/Prague"));

        int day = zdt.getDayOfMonth();
        int month = zdt.getMonthValue();
        int year = zdt.getYear();

        int hours = zdt.getHour();
        int minutes = zdt.getMinute();

        return new DateTime(day, month, year, hours, minutes);
    }

    public static DateTime parseEpoch(String str) {
        long epoch = Long.parseLong(str)*1_000;

        var calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epoch);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        return new DateTime(day, month, year, hours, minutes);
    }

    public static List<String> toShortenedInformedString(DateTime from, DateTime to) {
        DateTime now = now();

        if (to == NONE) {
            if (from.year != now.year) {
                String fromStr = from.toString();
                return List.of(fromStr);
            }

            if (from.month != now.month || from.day != now.day) {
                String fromStr = from.toYearlessString();
                return List.of(fromStr);
            }

            String fromStr = from.toTimeString();
            return List.of(fromStr);
        }

        if (from.year != to.year || to.year != now.year) {
            String fromStr = from.toString();

            return List.of(fromStr, to.toString());
        }
        if (from.month != to.month || from.day != to.day || to.month != now.month || to.day != now.day) {
            String fromStr = from.toYearlessString();

            return List.of(fromStr, to.toYearlessString());
        }

        String fromStr = from.toTimeString();

        return List.of(fromStr, to.toTimeString());
    }

    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.of(year, month, day, hours, minutes);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),"%02d.%02d.%d %02d:%02d", day, month, year, hours, minutes);
    }

    public String toYearlessString() {
        return String.format(Locale.getDefault(),"%02d.%02d. %02d:%02d", day, month, hours, minutes);
    }

    public String toTimeString() {
        return String.format(Locale.getDefault(),"%02d:%02d", hours, minutes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (this == NONE) {
            dest.writeByte((byte)0);
            return;
        }
        dest.writeByte((byte)1);

        dest.writeInt(day);
        dest.writeInt(month);
        dest.writeInt(year);
        dest.writeInt(hours);
        dest.writeInt(minutes);
    }
}
