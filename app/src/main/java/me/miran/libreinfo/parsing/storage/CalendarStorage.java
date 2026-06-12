package me.miran.libreinfo.parsing.storage;

import android.util.Log;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.miran.libreinfo.util.AppInputStream;

public class CalendarStorage {

    private final Map<Integer, CalendarEntry> serviceToCalendar;
    private final Map<Integer, Map<Date, ExceptionType>> exceptions;

    public static CalendarStorage parse(AppInputStream calendar, AppInputStream calendarDates) {
        try(calendar) {
            return parseInternal(calendar, calendarDates);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CalendarStorage parseInternal(AppInputStream calendarIs, AppInputStream calendarDatesIs) throws IOException {
        long ms = System.currentTimeMillis();

        List<CalendarEntry> entries = new ArrayList<>();
        Map<Integer, CalendarEntry> serviceToCalendar = new HashMap<>();

        while (calendarIs.readBoolean()) {
            int serviceId = calendarIs.readShort();

            Date from = Date.parse(calendarIs.readInt());
            Date to = Date.parse(calendarIs.readInt());

            int data = calendarIs.read();

            boolean monday = ((data >> 0) & 1) == 1;
            boolean tuesday = ((data >> 1) & 1) == 1;
            boolean wednesday = ((data >> 2) & 1) == 1;
            boolean thursday = ((data >> 3) & 1) == 1;
            boolean friday = ((data >> 4) & 1) == 1;
            boolean saturday = ((data >> 5) & 1) == 1;
            boolean sunday = ((data >> 6) & 1) == 1;

            CalendarEntry entry = new CalendarEntry(serviceId, monday, tuesday, wednesday, thursday, friday, saturday, sunday, from, to);

            entries.add(entry);
            serviceToCalendar.put(serviceId, entry);
        }


        Map<Integer, Map<Date, ExceptionType>> exceptions = new HashMap<>();

        while (calendarDatesIs.readBoolean()) {
            int serviceId = calendarDatesIs.readShort();
            Date date = Date.parse(calendarDatesIs.readInt());
            int exceptionTypeI = calendarDatesIs.read();

            ExceptionType type;
            if (exceptionTypeI == 1) {
                type = ExceptionType.ADDED;
            } else if (exceptionTypeI == 2) {
                type = ExceptionType.REMOVED;
            } else {
                throw new IllegalStateException();
            }

            exceptions.computeIfAbsent(serviceId, k -> new HashMap<>()).put(date, type);
        }

        System.out.println("took " + (System.currentTimeMillis() - ms) + "ms " + entries.size());


        return new CalendarStorage(serviceToCalendar, exceptions);
    }

    private CalendarStorage(Map<Integer, CalendarEntry> serviceToCalendar, Map<Integer, Map<Date, ExceptionType>> exceptions) {
        this.serviceToCalendar = serviceToCalendar;
        this.exceptions = exceptions;
    }

    public boolean available(Date date, int serviceId) {
        Map<Date, ExceptionType> map = exceptions.get(serviceId);

        if (map != null) {
            if (map.containsKey(date)) {
                ExceptionType type = map.get(date);

                return type == ExceptionType.ADDED;
            }
        }
        CalendarEntry entry = serviceToCalendar.get(serviceId);

        if (entry == null) {
            Log.w("CalendarStorage", "entry for "+serviceId+" is not present!");
            return false;
        }

        return entry.availableOn(date);
    }

    private enum ExceptionType {
        ADDED, REMOVED
    }

    public record CalendarEntry(int serviceId, boolean monday, boolean tuesday,
                                boolean wednesday, boolean thursday, boolean friday,
                                boolean saturday, boolean sunday,
                                Date from, Date to) {

        public boolean availableOn(Date date) {
            if (!date.isBetween(from, to)) return false;

            switch (date.getDayOfWeek()) {
                case MONDAY -> {
                    return monday;
                }
                case TUESDAY -> {
                    return tuesday;
                }
                case WEDNESDAY -> {
                    return wednesday;
                }
                case THURSDAY -> {
                    return thursday;
                }
                case FRIDAY -> {
                    return friday;
                }
                case SATURDAY -> {
                    return saturday;
                }
                case SUNDAY -> {
                    return sunday;
                }
            }
            throw new IllegalStateException();
        }

    }

    public record Date(int day, int month, int year, int dayOfWeek) {

        public Date(int day, int month, int year) {
            this(day, month, year, LocalDate.of(year, month, day).getDayOfWeek().getValue());
        }

        public DayOfWeek getDayOfWeek() {
            return DayOfWeek.of(dayOfWeek);
        }

        public static Date now() {
            LocalDateTime now = LocalDateTime.now();

            return new Date(now.getDayOfMonth(), now.getMonthValue(), now.getYear());
        }

        public static Date parse(String s) {
            int year = Integer.parseInt(s.substring(0, 4));
            int month = Integer.parseInt(s.substring(4, 6));
            int day = Integer.parseInt(s.substring(6));

            return new Date(day, month, year);
        }

        public static Date parse(int packed) {
            return new Date(packed&0xFF, (packed>>8)&0xFF,packed>>16);
        }

        public boolean isAfter(Date date) {
            if (year > date.year) return true;
            else if (year < date.year) return false;

            if (month > date.month) return true;
            else if (month < date.month) return false;

            return day >= date.day;
        }

        public boolean isBefore(Date date) {
            if (year < date.year) return true;
            else if (year > date.year) return false;

            if (month < date.month) return true;
            else if (month > date.month) return false;

            return day <= date.day;
        }

        public boolean isBetween(Date from, Date to) {
            return isAfter(from) && isBefore(to);
        }
    }

}
