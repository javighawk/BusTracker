package com.bustracker.gtfs;

import com.bustracker.bus.TripStop;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GTFSManager {
	
	private final URL url;
	private final GTFSStaticData staticData;
	private Map<String, PublishSubject<Set<TripStop>>> busStopSubscriptions = 
			new HashMap<>();
	private final Logger LOG = LoggerFactory.getLogger( GTFSManager.class );

	public GTFSManager( 
			String sourceURL, 
			Duration taskPeriod, 
			GTFSStaticData staticData,
			ScheduledExecutorService executorService ) {
		try {
			this.url = new URL( sourceURL );
		} catch( MalformedURLException e ) {
			throw new IllegalArgumentException( "Bad URL", e );
		}
		this.staticData = staticData;
		executorService.scheduleAtFixedRate( 
				this::task,
				0, 
				taskPeriod.getSeconds(), 
				TimeUnit.SECONDS );
	}
	
	private void task() {
		LOG.info( "Start GTFS retrieval task" );
		LOG.info( "Bus stops being tracked: {}", busStopSubscriptions.keySet() );
		FeedMessage feed;
		try {
			feed = FeedMessage.parseFrom( url.openStream() );
			Map<String, Set<TripStop>> updates = 
					getBusStopToTripStopsFromFeed( feed );
			LOG.info( "New updates for bus stops {}", updates.keySet() );
			notifySubscriptions( updates );
		} catch( IOException e ) {
			LOG.error( "Error on GTFS Task: {}", e.getMessage() );
		}
		LOG.info( "Done GTFS Task" );
	}
	
	private void notifySubscriptions( Map<String, Set<TripStop>> updates ) {
		updates.forEach( ( k, v ) -> busStopSubscriptions.get( k ).onNext( v ) );
	}

	private Map<String, Set<TripStop>> getBusStopToTripStopsFromFeed( 
			FeedMessage feed ) {
		Map<String, Set<TripStop>> ret = Maps.newHashMap();
		for( FeedEntity entity : feed.getEntityList() ) {
			if( entity.hasTripUpdate() ) {
				Map<String, Set<TripStop>> map =
						getBusStopIdToTripStopsFromTripUpdate( 
								entity.getTripUpdate() );
				addMap2ToMap1( ret, map );
			}
		}
		return ret;
	}
	
	private void addMap2ToMap1(
			Map<String, Set<TripStop>> map1, 
			Map<String, Set<TripStop>> map2) {
		map2.forEach( ( k, v ) -> {
			if( map1.containsKey( k ) ) {
				Set<TripStop> set = map1.get( k );
				set.addAll( v );
				map1.put( k, set );
			} else {
				map1.put( k, v );
			}
		});
	}

	private Map<String, Set<TripStop>> getBusStopIdToTripStopsFromTripUpdate(
			TripUpdate tripUpdate ) {
		Map<String, Set<TripStop>> ret = Maps.newHashMap();
		String tripId = tripUpdate.getTrip().getTripId();
		Optional<String> busNumberOpt =
				staticData.getBusNumberFromTrip( tripId );
		if( busNumberOpt.isPresent() ) {
			tripUpdate.getStopTimeUpdateList( ).stream( ).filter(
					stu1 -> busStopSubscriptions.containsKey(
							stu1.getStopId( ) ) ).forEach(
					stu -> addStopUpdateToMap(
							tripId, busNumberOpt.get(), stu, ret ) );
		} else {
			LOG.error(
					"Could not match tripId=" +
							tripId + " to a bus number (outdated GTFS data?)" );
		}
		return ret;
	}

	private void addStopUpdateToMap(
			String tripId,
			String busNumber,
			TripUpdate.StopTimeUpdate stopTimeUpdate,
			Map<String, Set<TripStop>> ret ) {
		LOG.info( "Adding update: busNumber={}, stopTimeUpdate={}",
				tripId, stopTimeUpdate );
		staticData.getScheduledArrivalTimeFromStopID(
				tripId, stopTimeUpdate.getStopId() ).ifPresent(
						time -> addToMap(
								ret, new TripStop(
										tripId,
										busNumber,
										stopTimeUpdate.getStopId(),
										time,
										Duration.ofSeconds(
												stopTimeUpdate
														.getArrival()
														.getDelay() ) ) ) );
	}

	private void addToMap(
			Map<String, Set<TripStop>> map,
			TripStop tripStop ) {
		String busStopId = tripStop.getBusStopId();
		Set<TripStop> tripStops = 
				map.containsKey( busStopId ) ? 
						map.get( busStopId ) : 
						Sets.newHashSet() ;
		tripStops.add( tripStop );
		map.put( busStopId, tripStops );
	}

	public Subscription subscribeToBusStopUpdates(
			String busStopId,
			Action1<? super Set<TripStop>> action ) {
	    LOG.info( "Subscribing bus stop ID {}", busStopId );
		PublishSubject<Set<TripStop>> subject = PublishSubject.create();
		busStopSubscriptions.put( busStopId, subject );
		return subject.asObservable().subscribe( action );
	}

	public void unsubscribeToBusStopUpdates( 
			String busStopId ) {
		if( busStopSubscriptions.containsKey( busStopId ) ) {
			busStopSubscriptions.remove( busStopId );
		}
	}

	public GTFSStaticData getStaticData() {
		return staticData;
	}
}
