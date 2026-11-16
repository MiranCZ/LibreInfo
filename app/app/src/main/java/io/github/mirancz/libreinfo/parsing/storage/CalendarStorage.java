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
        Map<Short, CalendarEntry> serviceToCalendar = new HashMap<>();

        Map<Short, Map<Date, ExceptionType>> exceptions = new HashMap<>();

        CalendarCodec.read(calendarIs,entry -> {
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

    public boolean available(Date date, short serviceId) {
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

}
