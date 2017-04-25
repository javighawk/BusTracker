package com.bustracker.bus;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import rx.subjects.PublishSubject;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class BusStop {

	private final String busStopId;
	private final Set<TripStop> allTripStops;
	private final PublishSubject<BusStop> tripsUpdateSubject = 
			PublishSubject.create();
	private final Set<TripStop> lastUpdatedTripStops = Sets.newHashSet();
	
	public BusStop( 
			String busStopId, Set<TripStop> tripStops ) {
		this.busStopId = busStopId;
		this.allTripStops = tripStops;
	}
	
	public void setNewUpdates( Set<TripStop> tripStops ) {
		System.out.println( "New updates for " + busStopId );
		System.out.println( tripStops );
		Map<String, Duration> tripIdToDelay = Maps.newHashMap();
	    tripStops.forEach(
	            ts -> tripIdToDelay.put( ts.getTripId(), ts.getDelay() ) );
	    for( TripStop ts : allTripStops ) {
	        if( tripStops.contains( ts ) ) {
	            ts.setDelay( tripIdToDelay.get( ts.getTripId() ) );
	            ts.setIsRealTime( true );
            } else {
	            ts.setDelay( Duration.ZERO );
	            ts.setIsRealTime( false );
            }
        }
	}

	public Optional<TripStop> getUpcomingBus( int busIndex ) {
		return new TreeSet<>( allTripStops )
				.stream( )
				.filter(
						ts -> ts.getRealArrivalTime( )
								.compareTo( LocalTime.now( ) ) >= 0 )
				.limit( busIndex + 1 )
				.max( TripStop::compareTo );
	}
	
	public String getBusStopId() {
		return busStopId;
	}

}
