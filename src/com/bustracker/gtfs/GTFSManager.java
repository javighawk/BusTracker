package com.bustracker.gtfs;

import java.util.Set;

import com.bustracker.bus.TripStop;

import rx.Observable;

public interface GTFSManager {
	
	Set<TripStop> getUpcomingTripsFromBusStop( String busStopId, int numberOfTrips );
	
	Observable<Void> getNewUpdatesEvents();
}
