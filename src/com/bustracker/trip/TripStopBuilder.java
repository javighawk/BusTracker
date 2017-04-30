package com.bustracker.trip;

import com.bustracker.trip.calendar.TripCalendar;

import java.time.Duration;

public class TripStopBuilder {

    private String tripId;
    private String busLine;
    private String busStopId;
    private Duration delay = Duration.ZERO;
    private TripCalendar calendar;
    private String scheduledArrivalString;

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
        this.scheduledArrivalString = scheduledArrival;
        return this;
    }

    public TripStop build() {
        return new TripStop(
                tripId,
                busLine,
                busStopId,
                scheduledArrivalString,
                calendar,
                delay);
    }
}