package com.bustracker.bus;

import com.bustracker.gtfs.GTFSManager;
import com.google.common.collect.Sets;

import java.util.Optional;
import java.util.Set;

public class BusStopManager {
	
	private final GTFSManager gtfsManager;
	private Set<BusStop> busStops = Sets.newHashSet();
	
	public BusStopManager( GTFSManager gtfsManager ) {
		this.gtfsManager = gtfsManager;
	}

	public void addBusStopToTrack( String busStopName ) {
		gtfsManager.getStaticData().getBusStopIDs( busStopName ).forEach( bs -> {
			Set<TripStop> trips =
					gtfsManager
							.getStaticData()
							.getAllScheduledTripStopsFromBusStopId(
									bs );
			busStops.add( new BusStop( bs, trips ) );
			gtfsManager.subscribeToBusStopUpdates(
					bs,
					tss -> onTripStopsUpdates( bs, tss ) );
		} );
	}

	public void removeBusStopToTrack( String busStopName ) {
		gtfsManager.getStaticData().getBusStopIDs( busStopName )
				.forEach( gtfsManager::unsubscribeToBusStopUpdates );
	}

	private void onTripStopsUpdates( 
			String busStopId, Set<TripStop> tss ) {
		getBusStop( busStopId )
		.ifPresent( bs -> bs.setNewUpdates( tss ) );
	}

	public Optional<BusStop> getBusStop( String busStopId ) {
		for( BusStop busStop : busStops ) {
			if( busStop.getBusStopId().equals( busStopId ) ) {
				return Optional.of( busStop );
			}
		}
		return Optional.empty();
	}
}
