package com.bustracker.trip;

import com.bustracker.trip.calendar.TripCalendar;
import com.bustracker.trip.thread.TripStopThreads;
import com.google.common.util.concurrent.Futures;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Future;

public class TripStop implements Comparable<TripStop> {

	private final int tripId;
	private final int busLine;
	private final int busStopId;
	private final LocalTime scheduledArrivalTime;
	private final TripCalendar calendar;
	private Duration delay;
	private boolean isRealTime = false;
	private Future<?> timeoutFuture =
			Futures.immediateFuture( null );

    public TripStop(
			int tripId,
			int busLine,
			int busStopId,
			LocalTime scheduledArrivalTime,
			TripCalendar calendar,
			Duration delay ) {
        this.tripId = tripId;
        this.busLine = busLine;
        this.busStopId = busStopId;
        this.calendar = calendar;
        this.delay = delay;
        this.scheduledArrivalTime = scheduledArrivalTime;
    }

	public TripStop(
			String tripId,
			String busLine,
			String busStopId,
			LocalTime scheduledArrivalTime,
			TripCalendar calendar,
			Duration delay ) {
		this(
				Integer.parseInt( tripId ),
				Integer.parseInt( busLine ),
				Integer.parseInt( busStopId ),
				scheduledArrivalTime,
				calendar,
				delay );
	}

	public int getTripId() {
		return tripId;
	}

	public int getBusLine() {
		return busLine;
	}
	
	public int getBusStopId() {
		return busStopId;
	}

	public LocalTime getScheduledArrivalTime() {
		return scheduledArrivalTime;
	}
	
	private LocalTime getRealArrivalTime() {
		return scheduledArrivalTime.plus( delay );
	}

	public LocalDateTime getRealArrivalDateTime() {
		return calendar.getNextOperatingDateTime( getRealArrivalTime() );
	}

	public void setDelay( Duration delay ) {
		this.delay = delay;
		this.isRealTime = true;
		if( !timeoutFuture.isDone() ) {
			timeoutFuture.cancel( true );
		}
		timeoutFuture = TripStopThreads.schedule( this::clearDelay );
	}

	private void clearDelay() {
		this.delay = Duration.ZERO;
		this.isRealTime = false;
	}

	public Duration getDelay( ) {
		return delay;
	}

	public boolean isRealTime() {
		return isRealTime;
	}

	@Override
	public boolean equals( Object obj ) {
		if( !(obj instanceof TripStop) ) {
			return false;
		}
		TripStop other = (TripStop) obj;

		return
				tripId == other.getTripId() &&
				busStopId == other.getBusStopId();
	}

	@Override
	public int compareTo( TripStop o ) {
		return getRealArrivalDateTime().compareTo(
				o.getRealArrivalDateTime() );
	}

	@Override
	public String toString() {
        return String.format( "TripStop=[tripId=%d, busLine=%d, busStopId=%d, " +
				"schedArrivalTime=%s, " +
				"realArrivalTime=%s, " +
				"calendar=%s, " +
				"delay=%d, realTime=%b]",
				tripId, busLine, busStopId,
                scheduledArrivalTime,
				getRealArrivalDateTime(),
				calendar,
				delay.getSeconds(),
                isRealTime );
	}

	public static TripStopBuilder builder() {
	    return new TripStopBuilder();
    }
}