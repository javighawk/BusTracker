package com.bustracker.gtfs;

import com.bustracker.trip.TripStop;

import java.util.Set;

/**
 * Created by Javier on 2017-05-15.
 */
public interface GTFSStaticData {

    Set<String> getBusStopIDs( String busStopName );

    Set<TripStop> getAllScheduledTripStopsFromBusStopId( String busStopId );

    String getTimeZone();
}
