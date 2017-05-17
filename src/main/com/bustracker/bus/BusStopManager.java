package com.bustracker.bus;

import com.bustracker.gtfs.GTFSManager;
import com.bustracker.trip.TripStop;
import com.bustracker.trip.TripStopUpdate;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BusStopManager {

    private final GTFSManager gtfsManager;
    private Set<BusStop> busStops = Sets.newHashSet( );
    private final PublishSubject<Void> addedBusStopSubject =
            PublishSubject.create();
    private final PublishSubject<Void> removedBusStopSubject =
            PublishSubject.create();
    private final Logger LOG = LoggerFactory.getLogger( BusStopManager.class );

    public BusStopManager( GTFSManager gtfsManager ) {
        this.gtfsManager = gtfsManager;
    }

    public void addBusStopToTrack( String busStopName ) {
        gtfsManager.getStaticData().getBusStopIDs( busStopName ).forEach(
                this::addBusStopIfNotExistent );
    }

    private void addBusStopIfNotExistent( String stopId ) {
        int stopIdInt = Integer.parseInt( stopId );
        for( BusStop bs : busStops ) {
            if( bs.getBusStopId() == stopIdInt ) {
                LOG.error( "Error adding busStopId={}, already being tracked" );
                return;
            }
        }
        Set<TripStop> trips = getAllTripStopFromBusStopId( stopId );
        addBusStopAndSubscibeForUpdates( stopIdInt, trips );
    }

    private void addBusStopAndSubscibeForUpdates(
            int stopId, Set<TripStop> trips ) {
        busStops.add( new BusStop( stopId, trips ) );
        gtfsManager.subscribeToBusStopUpdates(
                stopId,
                tripStopUpdates ->
                        onNewTripStopsUpdates(
                                stopId,
                                tripStopUpdates ) );
        addedBusStopSubject.onNext( null );
    }

    private Set<TripStop> getAllTripStopFromBusStopId( String stopId ) {
        return gtfsManager
                .getStaticData( )
                .getAllScheduledTripStopsFromBusStopId( stopId );
    }

    public void removeBusStopToTrack( String busStopName ) {
        gtfsManager.getStaticData( ).getBusStopIDs( busStopName )
                .forEach(
                        id -> gtfsManager.unsubscribeToBusStopUpdates(
                                Integer.parseInt( id ) ) );
        removedBusStopSubject.onNext( null );
    }

    private void onNewTripStopsUpdates(
            int busStopId, Set<TripStopUpdate> tripStopUpdates ) {
        getBusStop( busStopId )
                .ifPresent( busStop -> busStop.setNewUpdates( tripStopUpdates ) );
    }

    private Optional<BusStop> getBusStop( int busStopId ) {
        for( BusStop busStop : busStops ) {
            if( busStop.getBusStopId() == busStopId ) {
                return Optional.of( busStop );
            }
        }
        return Optional.empty( );
    }

    public Optional<BusStop> getNextBusStop( Optional<BusStop> busStopOpt ) {
        int busStopId =
                busStopOpt.map( BusStop::getBusStopId ).orElse( -1 );
        List<BusStop> busStopsList = busStops.stream()
                .sorted( Comparator.comparingInt( BusStop::getBusStopId ) )
                .collect( Collectors.toList() );

        Optional<BusStop> ret = busStopsList.stream()
                .filter( bs -> bs.getBusStopId() > busStopId )
                .findFirst();

        return ret.isPresent() ? ret : busStopsList.stream().findFirst();
    }

    public Observable<Void> getAddedBusStopEvents() {
        return addedBusStopSubject.asObservable();
    }

    public Observable<Void> getRemovedBusStopEvents() {
        return removedBusStopSubject.asObservable();
    }
}
