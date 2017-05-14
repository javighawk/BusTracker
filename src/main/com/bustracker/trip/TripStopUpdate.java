package com.bustracker.trip;

import java.time.Duration;

/**
 * Created by Javier on 2017-04-29.
 */
public class TripStopUpdate {

    private final int tripId;
    private final int busStopId;
    private final Duration delay;

    public TripStopUpdate(
            int tripId,
            int busStopId,
            Duration delay ) {
        this.tripId = tripId;
        this.busStopId = busStopId;
        this.delay = delay;
    }

    public TripStopUpdate(
            String tripId,
            String busStopId,
            Duration delay ) {
        this(
                Integer.parseInt( tripId ),
                Integer.parseInt( busStopId ),
                delay );
    }

    public int getTripId() {
        return tripId;
    }

    public int getBusStopId() {
        return busStopId;
    }

    public Duration getDelay( ) {
        return delay;
    }

    @Override
    public String toString() {
        return String.format( "TripStopUpdate=" +
                        "[tripId=%d, busStopId=%d, " +
                        "delay=%d]",
                tripId, busStopId, delay.getSeconds() );
    }

    public boolean equalToTripStop( TripStop obj ) {
        return tripId == obj.getTripId() &&
                busStopId == obj.getBusStopId();
    }
}
