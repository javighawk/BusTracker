package com.bustracker.bus;

import com.bustracker.gtfs.GTFSManager;
import com.bustracker.trip.TripStop;
import com.bustracker.trip.TripStopUpdate;
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
		gtfsManager.getStaticData().getBusStopIDs( busStopName ).forEach(
				stopId -> {
					Set<TripStop> trips =
							gtfsManager
									.getStaticData()
									.getAllScheduledTripStopsFromBusStopId( stopId );
					busStops.add( new BusStop( stopId, trips ) );
					gtfsManager.subscribeToBusStopUpdates(
							stopId,
							tripStopUpdates ->
									onNewTripStopsUpdates(
											stopId,
											tripStopUpdates ) );
		} );
	}

	public void removeBusStopToTrack( String busStopName ) {
		gtfsManager.getStaticData().getBusStopIDs( busStopName )
				.forEach( gtfsManager::unsubscribeToBusStopUpdates );
	}

	private void onNewTripStopsUpdates(
			String busStopId, Set<TripStopUpdate> tripStopUpdates ) {
		getBusStop( busStopId )
		.ifPresent( busStop -> busStop.setNewUpdates( tripStopUpdates ) );
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
