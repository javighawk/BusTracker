package com.bustracker.trip;

import com.bustracker.trip.calendar.TripCalendar;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class TripStopBuilder {

    private String tripId;
    private String busLine;
    private String busStopId;
    private Duration delay = Duration.ZERO;
    private TripCalendar calendar;
    private LocalTime scheduledArrival;
    private boolean isArrivalAfterMidnight;

    TripStopBuilder() {
    }

    public TripStopBuilder withTripId(String tripId) {
        this.tripId = tripId;
        return this;
    }

    public TripStopBuilder withBusLine(String busLine) {
        this.busLine = busLine;
        return this;
    }

    public TripStopBuilder withBusStopId(String busStopId) {
        this.busStopId = busStopId;
        return this;
    }

    public TripStopBuilder withTripCalendar(
            TripCalendar calendar) {
        this.calendar = calendar;
        return this;
    }

    public TripStopBuilder withDelay(Duration delay) {
        this.delay = delay;
        return this;
    }

    public TripStopBuilder withScheduledArrival(String scheduledArrival) {
        this.scheduledArrival = parseTime( scheduledArrival );
        return this;
    }

    private LocalTime parseTime( String time ) {
        try {
            return LocalTime.parse( time );
        } catch( DateTimeParseException e ) {
            isArrivalAfterMidnight = true;
            int hour = Integer.parseInt( time.substring( 0,2 ) ) - 24;
            return LocalTime.parse(
                    String.format( "%02d", hour ) + time.substring( 2 ) );
        }
    }

    public TripStop build() {
        if( isArrivalAfterMidnight ) {
            calendar = calendar.getCopyWithDayShift();
        }
        return new TripStop(
                tripId,
                busLine,
                busStopId,
                scheduledArrival,
                calendar,
                delay);
    }
}