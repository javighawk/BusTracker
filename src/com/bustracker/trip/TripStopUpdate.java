package com.bustracker.trip;

import java.time.Duration;

/**
 * Created by Javier on 2017-04-29.
 */
public class TripStopUpdate {

    private final String tripId;
    private final String busStopId;
    private final Duration delay;

    public TripStopUpdate(
            String tripId,
            String busStopId,
            Duration delay ) {
        this.tripId = tripId;
        this.busStopId = busStopId;
        this.delay = delay;
    }

    public String getTripId() {
        return tripId;
    }

    public String getBusStopId() {
        return busStopId;
    }

    public Duration getDelay( ) {
        return delay;
    }

    @Override
    public String toString() {
        return String.format( "TripStopUpdate=" +
                        "[tripId=%s, busStopId=%s, " +
                        "delay=%d]",
                tripId, busStopId, delay.getSeconds() );
    }

    public boolean equalToTripStop( TripStop obj ) {
        return tripId.equals( obj.getTripId() ) &&
                busStopId.equals( obj.getBusStopId() );
    }
}
