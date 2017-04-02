package com.bustracker.gtfs;

import java.util.Set;

import com.bustracker.bus.TripStop;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

public interface GTFSManager {
	
	Subscription subscribeToBusStopUpdates( 
			String busStopId,  Action1<? super Set<TripStop>> action );
	
	void unsubscribeToBusStopUpdates( String busStopId );
}
