package com.bustracker.bus;

import java.time.Duration;
import java.time.LocalTime;

public class TripStopBuilder {

    private String tripId;
    private String busLine;
    private String busStopId;
    private LocalTime scheduledArrival;
    private Duration delay = Duration.ZERO;
    private String scheduledArrivalTime;

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

    public TripStopBuilder withScheduledArrival(LocalTime scheduledArrival) {
        this.scheduledArrival = scheduledArrival;
        return this;
    }

    public TripStopBuilder withDelay(Duration delay) {
        this.delay = delay;
        return this;
    }

    public TripStopBuilder withScheduledArrivalTime(String scheduledArrivalTime) {
        this.scheduledArrivalTime = scheduledArrivalTime;
        return this;
    }

    public TripStop build() {
        return new TripStop(
                tripId,
                busLine,
                busStopId,
                scheduledArrival,
                delay);
    }
}