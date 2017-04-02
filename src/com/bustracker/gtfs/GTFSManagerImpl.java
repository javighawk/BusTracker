package com.bustracker.gtfs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.bustracker.bus.TripStop;
import com.bustracker.bus.TripStopImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;

import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

public class GTFSManagerImpl implements GTFSManager {
	
	private final URL url;
	private final GTFSStaticData staticData;
	private Map<String, PublishSubject<Set<TripStop>>> busStopSubscriptions = 
			new HashMap<>();

	public GTFSManagerImpl( 
			String sourceURL, 
			Duration taskPeriod, 
			GTFSStaticData staticData,
			ScheduledExecutorService executorService )
					throws MalformedURLException {
		this.url = new URL(sourceURL);
		this.staticData = staticData;
		executorService.scheduleAtFixedRate( 
				this::task,
				0, 
				taskPeriod.getSeconds(), 
				TimeUnit.SECONDS );
	}
	
	private void task() {
		FeedMessage feed;
		try {
			feed = FeedMessage.parseFrom( url.openStream() );
			Map<String, Set<TripStop>> updates = 
					getTripStopsFromFeed(feed);
			notifySubscriptions( updates );
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}
	
	private void notifySubscriptions( Map<String, Set<TripStop>> updates ) {
		updates.forEach( ( k, v ) -> busStopSubscriptions.get( k ).onNext( v ) );
	}

	private Map<String, Set<TripStop>> getTripStopsFromFeed( FeedMessage feed ) {
		Map<String, Set<TripStop>> ret = Maps.newHashMap();
		for( FeedEntity entity : feed.getEntityList() ) {
			if( entity.hasTripUpdate() ) {								  
				Map<String, Set<TripStop>> map =
						getBusStopToTripStopsFromTripUpdate( 
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

	private Map<String, Set<TripStop>> getBusStopToTripStopsFromTripUpdate(
			TripUpdate tripUpdate ) {
		Map<String, Set<TripStop>> ret = Maps.newHashMap();
		String tripID = tripUpdate.getTrip().getTripId();
		String busNumber = staticData.getBusNumberFromRoute( 
				tripUpdate.getTrip().getRouteId() );
		if( busNumber != null ) {
			tripUpdate
			.getStopTimeUpdateList()
			.stream()
			.filter( stu -> busStopSubscriptions.containsKey( stu.getStopId() ) )
			.map( stu -> new TripStopImpl( 
					tripID,
					busNumber, 
					stu.getStopId(),
					staticData.getScheduledArrivalTimeFromStopID(
							tripID, stu.getStopId() ),
					stu.getArrival().getDelay() ) )
			.forEach( ts -> addToMap( ret, ts ) );
		}
		return ret;
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

	@Override
	public Subscription subscribeToBusStopUpdates(
			String busStopId, Action1<? super Set<TripStop>> action ) {
		PublishSubject<Set<TripStop>> subject = PublishSubject.create();
		busStopSubscriptions.put( busStopId, subject );
		return subject.asObservable().subscribe( action );
	}

	@Override
	public void unsubscribeToBusStopUpdates( String busStopId ) {
		if( busStopSubscriptions.containsKey( busStopId ) ) {
			busStopSubscriptions.remove( busStopId );
		}
	}
	
}
