package com.bustracker.trip.calendar;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

/**
 * Created by Javier on 2017-04-30.
 */
public class TripCalendar {

    private final String calendarId;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Set<DayOfWeek> operatingWeekdays;
    private final TripCalendarException exception;

    TripCalendar(
            String calendarId,
            LocalDate startDate,
            LocalDate endDate,
            Set<DayOfWeek> operatingWeekdays,
            TripCalendarException exception ) {
        this.calendarId = calendarId;
        this.startDate = LocalDateTime.of( startDate, LocalTime.MIN );
        this.endDate = LocalDateTime.of( endDate, LocalTime.MAX );
        this.operatingWeekdays = operatingWeekdays;
        this.exception = exception;
        assertOperatingWeekdays();
    }

    private void assertOperatingWeekdays() {
        if( operatingWeekdays.size() == 0 ) {
            throw new IllegalStateException(
                    "No operating weekdays on CalendarId=" + calendarId );
        }
    }

    public boolean isActiveOn( LocalDateTime dateTime ) {
        LocalDate date = dateTime.toLocalDate( );
        if( exception.isServiceAdded( date ) ) {
            return true;
        }
        if( exception.isServiceRemoved( date ) ) {
            return false;
        }
        return startDate.isBefore( dateTime ) &&
                endDate.isAfter( dateTime ) &&
                operatingWeekdays.contains( dateTime.getDayOfWeek() );
    }

    public boolean isTerminatedOn( LocalDateTime dateTime ) {
        if( exception.isServiceAddedOnOrAfter( dateTime.toLocalDate() ) ) {
            return false;
        }
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
                "Calendar=[startDate=%s, endDate=%s, " +
                        "DaysOfWeek=%s, Exception=%s]",
                startDate.toLocalDate(),
                endDate .toLocalDate(),
                operatingWeekdays,
                exception );
    }

    public static TripCalendarBuilder builder() {
        return new TripCalendarBuilder();
    }
}
