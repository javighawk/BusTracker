package com.bustracker.bus;

import java.util.Set;

import rx.Observable;

public interface BusStop {
	
	void setNewUpdates( Set<TripStop> tripStops );

	TripStop getUpcomingBus( int busIndex );
	
	Observable<TripStop> getNewUpdatesEvents();
}
