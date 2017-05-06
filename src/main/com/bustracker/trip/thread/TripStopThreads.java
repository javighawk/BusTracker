package com.bustracker.trip.thread;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Javier on 2017-04-29.
 */
public final class TripStopThreads {

    private static final ScheduledExecutorService INSTANCE =
            Executors.newScheduledThreadPool( 20 );

    public static ScheduledFuture<?> schedule( Runnable command ) {
        return INSTANCE.schedule(
                command, 1, TimeUnit.MINUTES );
    }

    private TripStopThreads() {
        throw new UnsupportedOperationException();
    }
}
