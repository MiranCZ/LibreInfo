package io.github.mirancz.libreinfo.parsing.storage;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.mirancz.libreinfo.codec.CalendarCodec;
import io.github.mirancz.libreinfo.codec.CalendarDatesCodec;
import io.github.mirancz.libreinfo.codec.util.AppInputStream;
import io.github.mirancz.libreinfo.exception.AppException;
import io.github.mirancz.libreinfo.model.CalendarDate;
import io.github.mirancz.libreinfo.model.CalendarEntry;
import io.github.mirancz.libreinfo.model.types.Date;
import io.github.mirancz.libreinfo.util.AppLog;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class CalendarStorage implements AppStorage {

    private final Map<Short, CalendarEntry> serviceToCalendar;
    private final Map<Short, Map<Date, ExceptionType>> exceptions;

    public static CalendarStorage parse(AppInputStream calendar, AppInputStream calendarDates) throws AppException {
        try {
            return parseInternal(calendar, calendarDates);
        } catch (IOException e) {
            throw AppException.dataLoad(e);
        }
    }

    public static CalendarStorage parseInternal(AppInputStream calendarIs, AppInputStream calendarDatesIs) throws IOException {
//        List<CalendarEntry> entries = new ArrayList<>();
        Map<Short, CalendarEntry> serviceToCalendar = new HashMap<>();

        Map<Short, Map<Date, ExceptionType>> exceptions = new HashMap<>();

        CalendarCodec.read(calendarIs,entry -> {
//            entries.add(entry);
            serviceToCalendar.put(entry.getServiceId(), entry);

            return Unit.INSTANCE;
        });

        CalendarDatesCodec.read(calendarDatesIs, calDate -> {
            ExceptionType type;
            if (calDate.getExceptionType() == 1) {
                type = ExceptionType.ADDED;
            } else if (calDate.getExceptionType() == 2) {
                type = ExceptionType.REMOVED;
            } else {
                throw new IllegalStateException();
            }

            exceptions.computeIfAbsent(calDate.getServiceId(), k -> new HashMap<>()).put(calDate.getDate(), type);

            return Unit.INSTANCE;
        });


        return new CalendarStorage(serviceToCalendar, exceptions);
    }

    private CalendarStorage(Map<Short, CalendarEntry> serviceToCalendar, Map<Short, Map<Date, ExceptionType>> exceptions) {
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
            AppLog.w("entry for "+serviceId+" is not present!");
            return false;
        }

        return entry.availableOn(date);
    }

    private enum ExceptionType {
        ADDED, REMOVED
    }

//    public record CalendarEntry(int serviceId, boolean monday, boolean tuesday,
//                                boolean wednesday, boolean thursday, boolean friday,
//                                boolean saturday, boolean sunday,
//                                Date from, Date to) {
//
//        public boolean availableOn(Date date) {
//            if (!date.isBetween(from, to)) return false;
//            switch (date.getDayOfWeek()) {
//                case MONDAY -> {
//                    return monday;
//                }
//                case TUESDAY -> {
//                    return tuesday;
//                }
//                case WEDNESDAY -> {
//                    return wednesday;
//                }
//                case THURSDAY -> {
//                    return thursday;
//                }
//                case FRIDAY -> {
//                    return friday;
//                }
//                case SATURDAY -> {
//                    return saturday;
//                }
//                case SUNDAY -> {
//                    return sunday;
//                }
//            }
//            throw new IllegalStateException();
//        }
//
//    }

//    public record Date(int day, int month, int year, int dayOfWeek) {
//
//        public Date(int day, int month, int year) {
//            this(day, month, year, LocalDate.of(year, month, day).getDayOfWeek().getValue());
//        }
//
//        public DayOfWeek getDayOfWeek() {
//            return DayOfWeek.of(dayOfWeek);
//        }
//
//        public static Date now() {
//            LocalDateTime now = LocalDateTime.now();
//
//            return new Date(now.getDayOfMonth(), now.getMonthValue(), now.getYear());
//        }
//
//        public static Date parse(String s) {
//            int year = Integer.parseInt(s.substring(0, 4));
//            int month = Integer.parseInt(s.substring(4, 6));
//            int day = Integer.parseInt(s.substring(6));
//
//            return new Date(day, month, year);
//        }
//
//        public static Date parse(int packed) {
//            return new Date(packed&0xFF, (packed>>8)&0xFF,packed>>16);
//        }
//
//        public boolean isAfter(Date date) {
//            if (year > date.year) return true;
//            else if (year < date.year) return false;
//
//            if (month > date.month) return true;
//            else if (month < date.month) return false;
//
//            return day >= date.day;
//        }
//
//        public boolean isBefore(Date date) {
//            if (year < date.year) return true;
//            else if (year > date.year) return false;
//
//            if (month < date.month) return true;
//            else if (month > date.month) return false;
//
//            return day <= date.day;
//        }
//
//        public boolean isBetween(Date from, Date to) {
//            return isAfter(from) && isBefore(to);
//        }
//    }

}
