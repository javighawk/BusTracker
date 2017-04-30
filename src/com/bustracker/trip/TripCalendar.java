package com.bustracker.trip;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Created by Javier on 2017-04-30.
 */
public class TripCalendar {

    private final String calendarId;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Set<DayOfWeek> operatingWeekdays;

    public TripCalendar(
            String calendarId,
            LocalDate startDate,
            LocalDate endDate,
            Set<DayOfWeek> operatingWeekdays ) {
        this.calendarId = calendarId;
        this.startDate = LocalDateTime.of( startDate, LocalTime.MIN );
        this.endDate = LocalDateTime.of( endDate, LocalTime.MAX );
        this.operatingWeekdays = operatingWeekdays;
        assertOperatingWeekdays();
    }

    private void assertOperatingWeekdays() {
        if( operatingWeekdays.size() == 0 ) {
            throw new IllegalStateException(
                    "No operating weekdays on CalendarId=" + calendarId );
        }
    }

    public TripCalendar(
            String calendarId,
            String startDate,
            String endDate,
            Set<DayOfWeek> operatingWeekdays ) {
        this(
                calendarId,
                parseDate( startDate ),
                parseDate( endDate ),
                operatingWeekdays );

    }

    private static LocalDate parseDate( String date ) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern( "yyyyMMdd" );
        return LocalDate.parse( date, formatter );
    }

    public boolean isActiveOn( LocalDateTime dateTime ) {
        return startDate.isBefore( dateTime ) &&
                endDate.isAfter( dateTime ) &&
                operatingWeekdays.contains( dateTime.getDayOfWeek() );
    }

    public boolean isTerminatedOn( LocalDateTime dateTime ) {
        return endDate.isBefore( dateTime );
    }

    public LocalDateTime getNextOperatingDateTime( LocalTime time ) {
        LocalDateTime nextDateTime = LocalDateTime.of( LocalDate.now(), time );
        LocalDateTime now = LocalDateTime.now();
        while( now.isAfter( nextDateTime ) ||
                !isActiveOn( nextDateTime ) ) {
            nextDateTime = nextDateTime.plus( Duration.ofDays( 1 ) );
            if( isTerminatedOn( nextDateTime ) ) {
                return LocalDateTime.MAX;
            }
        }
        return nextDateTime;
    }

    @Override
    public String toString() {
        return String.format(
                "Calendar=[startDate=%s, endDate=%s, DaysOfWeek=%s]",
                startDate.toLocalDate(),
                endDate .toLocalDate(),
                operatingWeekdays );
    }
}
