package com.bustracker.gtfs;

import com.bustracker.trip.TripStopUpdate;
import rx.Subscription;
import rx.functions.Action1;

import java.util.Set;

/**
 * Created by Javier on 2017-05-15.
 */
public interface GTFSManager {

    Subscription subscribeToBusStopUpdates(
            int busStopId, Action1<? super Set<TripStopUpdate>> action );

    void unsubscribeToBusStopUpdates( int busStopId );

    GTFSStaticData getStaticData();
}
