package com.bustracker.gtfs;

import com.bustracker.trip.TripStopUpdate;
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
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GTFSManager {
	
	private final URL url;
	private final GTFSStaticData staticData;
	private Map<String, PublishSubject<Set<TripStopUpdate>>> busStopSubscriptions =
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
		try {
			FeedMessage feed = getFeedMessageFromOnlineFeed();
			Set<TripStopUpdate> tripStopUpdates =
					getTripStopUpdatesFromFeedMessage( feed );
			notifyAllSubscribers( tripStopUpdates );
		} catch( IOException e ) {
			LOG.error( "Error on GTFS Task: {}", e.getMessage() );
		}
		LOG.info( "Done GTFS Task" );
	}

	private FeedMessage getFeedMessageFromOnlineFeed() throws IOException {
		LOG.info( "Download feed message" );
		return FeedMessage.parseFrom( url.openStream() );
	}

	private void notifyAllSubscribers( Set<TripStopUpdate> tripStopUpdates ) {
	    LOG.info( "Notify bus stops IDs=" + busStopSubscriptions.keySet() );
	    busStopSubscriptions.forEach( ( bsid, subscription ) ->
				notifySubscriber( bsid, subscription, tripStopUpdates ) );
	}

	private void notifySubscriber(
			String busStopId,
			PublishSubject<Set<TripStopUpdate>> subscription,
			Set<TripStopUpdate> tripStopUpdates ) {
		Set<TripStopUpdate> updates = tripStopUpdates
				.stream( )
				.filter( tsu -> tsu.getBusStopId( ).equals( busStopId ) )
				.collect( Collectors.toSet( ) );
		subscription.onNext( updates );
	}

	private Set<TripStopUpdate> getTripStopUpdatesFromFeedMessage(
			FeedMessage feed ) {
		Set<TripStopUpdate> tripStopUpdates = Sets.newHashSet();
		for( FeedEntity entity : feed.getEntityList() ) {
			if( entity.hasTripUpdate() ) {
				tripStopUpdates.addAll(
						getTripStopUpdatesFromGTFSTripUpdate(
								entity.getTripUpdate() ) );
			}
		}
		return tripStopUpdates;
	}
	
	private Set<TripStopUpdate> getTripStopUpdatesFromGTFSTripUpdate(
			TripUpdate tripUpdate ) {
		String tripId = tripUpdate.getTrip().getTripId();
		return tripUpdate.getStopTimeUpdateList().stream().filter(
				stu1 -> busStopSubscriptions.containsKey(
						stu1.getStopId() ) )
				.map( stu -> new TripStopUpdate(
						tripId,
						stu.getStopId(),
						Duration.ofSeconds( stu.getArrival().getDelay() ) ) )
				.collect( Collectors.toSet() );
	}

	public Subscription subscribeToBusStopUpdates(
			String busStopId,
			Action1<? super Set<TripStopUpdate>> action ) {
	    LOG.info( "Subscribing bus stop ID {}", busStopId );
		PublishSubject<Set<TripStopUpdate>> subject = PublishSubject.create();
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
