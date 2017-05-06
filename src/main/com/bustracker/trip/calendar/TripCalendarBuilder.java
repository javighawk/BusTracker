package com.bustracker.trip.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Created by Javier on 2017-04-30.
 */
public class TripCalendarBuilder {

    private String calendarId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<DayOfWeek> operatingWeekdays;

    private TripCalendarException exception = new TripCalendarException();

    TripCalendarBuilder() {
    }

    public TripCalendarBuilder withCalendarId(String calendarId) {
        this.calendarId = calendarId;
        return this;
    }

    public TripCalendarBuilder withStartDate(String startDate) {
        this.startDate = parseDate( startDate );
        return this;
    }

    public TripCalendarBuilder withEndDate(String endDate) {
        this.endDate = parseDate( endDate );
        return this;
    }

    public TripCalendarBuilder withOperatingWeekdays(
            Set<DayOfWeek> operatingWeekdays) {
        this.operatingWeekdays = operatingWeekdays;
        return this;
    }

    public TripCalendarBuilder withServiceAddedExceptionDate(
            String date ) {
        this.exception.addServiceAddedDate( parseDate( date ) );
        return this;
    }

    public TripCalendarBuilder withServiceRemovedExceptionDate(
            String date ) {
        this.exception.addServiceRemovedDate( parseDate( date ) );
        return this;
    }

    private LocalDate parseDate( String date ) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern( "yyyyMMdd" );
        return LocalDate.parse( date, formatter );
    }

    public TripCalendar build() {
        return new TripCalendar(
                calendarId,
                startDate,
                endDate ,
                operatingWeekdays,
                exception);
    }
}