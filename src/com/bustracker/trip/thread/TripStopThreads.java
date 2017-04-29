package com.bustracker.trip.thread;

import java.util.concurrent.*;

/**
 * Created by Javier on 2017-04-29.
 */
public final class TripStopThreads {

    private static final ScheduledExecutorService INSTANCE =
            Executors.newScheduledThreadPool( 20 );

    public static ScheduledFuture<?> schedule( Runnable command ) {
        return INSTANCE.scheduleAtFixedRate(
                command,
                0, 1, TimeUnit.MINUTES );
    }

    private TripStopThreads() {
        throw new UnsupportedOperationException();
    }
}
