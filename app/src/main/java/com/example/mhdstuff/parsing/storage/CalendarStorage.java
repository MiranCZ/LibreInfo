package com.example.mhdstuff.parsing.storage;

import android.util.Log;

import com.example.mhdstuff.util.Csv;
import com.example.mhdstuff.util.CsvHelper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarStorage {

    private final Map<Integer, CalendarEntry> serviceToCalendar;
    private final Map<Integer, Map<Date, ExceptionType>> exceptions;

    public static CalendarStorage parse(String calendar, String calendarDates) {
        try {
            return parseInternal(calendar, calendarDates);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CalendarStorage parseInternal(String calendarStr, String calendarDatesStr) throws IOException {
        Csv calendarCsv = CsvHelper.parseCsvEntries(calendarStr);

        List<CalendarEntry> entries = new ArrayList<>();
        Map<Integer, CalendarEntry> serviceToCalendar = new HashMap<>();


        System.out.println(calendarCsv.getLines().size());
        long ms = System.currentTimeMillis();
        for (Csv.CsvLine line : calendarCsv.getLines()) {
            int serviceId = line.getInt("service_id");
            boolean monday = line.getBoolean("monday");
            boolean tuesday = line.getBoolean("tuesday");
            boolean wednesday = line.getBoolean("wednesday");
            boolean thursday = line.getBoolean("thursday");
            boolean friday = line.getBoolean("friday");
            boolean saturday = line.getBoolean("saturday");
            boolean sunday = line.getBoolean("sunday");

            Date from = Date.parse(line.get("start_date"));
            Date to = Date.parse(line.get("end_date"));

            CalendarEntry entry = new CalendarEntry(serviceId, monday, tuesday, wednesday, thursday, friday, saturday, sunday, from, to);
            entries.add(entry);
            serviceToCalendar.put(serviceId, entry);
        }


        Map<Integer, Map<Date, ExceptionType>> exceptions = new HashMap<>();
        Csv exceptionCsv = CsvHelper.parseCsvEntries(calendarDatesStr);

        for (Csv.CsvLine line : exceptionCsv.getLines()) {
            int serviceId = line.getInt("service_id");
            Date date = Date.parse(line.get("date"));
            int exceptionTypeI = line.getInt("exception_type");
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

            LocalDateTime dt = LocalDateTime.now();

            switch (dt.getDayOfWeek()) {
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

    public record Date(int day, int month, int year) {

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
