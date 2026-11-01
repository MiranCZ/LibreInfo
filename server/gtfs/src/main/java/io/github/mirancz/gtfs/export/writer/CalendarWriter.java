package io.github.mirancz.gtfs.export.writer;

import io.github.mirancz.gtfs.ParsedGtfs;
import io.github.mirancz.gtfs.export.Writer;
import io.github.mirancz.gtfs.ids.CalendarDateEntry;
import io.github.mirancz.gtfs.ids.CalendarEntry;
import io.github.mirancz.util.CheckedOutputStream;

import java.io.IOException;
import java.util.function.Function;

public class CalendarWriter implements Writer {

    @Override
    public void write(ParsedGtfs gtfs, Function<String, CheckedOutputStream> outputProvider) {
        try (var os = outputProvider.apply("calendar")) {
            this.writeCalendar(gtfs, os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (var os = outputProvider.apply("calendar_dates")) {
            this.writeCalendarDates(gtfs, os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeCalendar(ParsedGtfs gtfs, CheckedOutputStream os) throws IOException {
        var calendar = gtfs.calendar();

        os.writeInt(calendar.size());

        for (CalendarEntry e : calendar) {
            byte data = 0;
            if (e.monday() == 1)    data |= 1 << 0;
            if (e.tuesday() == 1)   data |= 1 << 1;
            if (e.wednesday() == 1) data |= 1 << 2;
            if (e.thursday() == 1)  data |= 1 << 3;
            if (e.friday() == 1)    data |= 1 << 4;
            if (e.saturday() == 1)  data |= 1 << 5;
            if (e.sunday() == 1)    data |= 1 << 6;

            os.writeShort(e.service_id());
            os.writeInt(parseDate(e.start_date()));
            os.writeInt(parseDate(e.end_date()));
            os.writeByte(data);
        }
    }

    private void writeCalendarDates(ParsedGtfs gtfs, CheckedOutputStream os) throws IOException {
        var calendarDates = gtfs.calendarDates();

        os.writeInt(calendarDates.size());

        for (CalendarDateEntry e : calendarDates) {
            os.writeShort(e.service_id());
            os.writeInt(parseDate(e.date()));
            os.writeByte(e.exception_type());
        }
    }

    private static int parseDate(String date) {
        int day = Integer.parseInt(date.substring(date.length()-2));
        int month = Integer.parseInt(date.substring(date.length()-4, date.length()-2));

        int year = Integer.parseInt(date.substring(0, 4));

        return (year << 16) | (month<<8) | day;
    }


}
