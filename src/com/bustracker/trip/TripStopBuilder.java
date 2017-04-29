package com.bustracker.trip;

import com.google.common.collect.Sets;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.Set;

public class TripStopBuilder {

    private String tripId;
    private String busLine;
    private String busStopId;
    private Duration delay = Duration.ZERO;
    private Set<DayOfWeek> operatingWeekdays = Sets.newHashSet();
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

    public TripStopBuilder withOperatingWeekdays(
            Set<DayOfWeek> operatingWeekdays) {
        this.operatingWeekdays = operatingWeekdays;
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
                operatingWeekdays,
                delay);
    }
}