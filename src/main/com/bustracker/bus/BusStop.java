package com.bustracker.bus;

import com.bustracker.trip.TripStop;
import com.bustracker.trip.TripStopUpdate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.subjects.PublishSubject;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BusStop {

	private final String busStopId;
	private final Set<TripStop> allTripStops;
	private final PublishSubject<BusStop> tripsUpdateSubject = 
			PublishSubject.create();
	private final Set<TripStop> lastUpdatedTripStops = Sets.newHashSet();
    private final Logger LOG = LoggerFactory.getLogger( BusStop.class );
	
	public BusStop( 
			String busStopId, Set<TripStop> tripStops ) {
		this.busStopId = busStopId;
		this.allTripStops = tripStops;
	}
	
	public void setNewUpdates( Set<TripStopUpdate> tripStopUpdates ) {
	    LOG.info( "New updates for busStopId={}, tripStopUpdates={}",
                busStopId, tripStopUpdates );
        tripStopUpdates.forEach( tsu ->
            allTripStops.stream()
                    .filter( tsu::equalToTripStop )
                    .forEach( tripStop -> {
                        tripStop.setDelay( tsu.getDelay() );
                        LOG.info( "Updating delay on tripStop={}", tripStop );
                    } ) );
	}

	public Optional<TripStop> getUpcomingBus( int busIndex ) {
	    // Cannot use Stream filter method here because then, if two buses
        // arrive at the same time, only one appears. Need to do the
        // filtering manually
        List<TripStop> filteredTripStops = Lists.newArrayList();
		LocalDateTime now = LocalDateTime.now( );
		for( TripStop t : allTripStops ) {
			if( t.getRealArrivalDateTime().isAfter( now ) ) {
                filteredTripStops.add( t );
            }
        }
        if( filteredTripStops.size() > busIndex ) {
	        Collections.sort( filteredTripStops );
            return Optional.of( filteredTripStops.get( busIndex ) );
        }
        return Optional.empty();
	}

	public String getBusStopId() {
		return busStopId;
	}

}
