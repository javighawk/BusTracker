package com.bustracker.trip;

import com.bustracker.trip.calendar.TripCalendar;
import com.bustracker.trip.thread.TripStopThreads;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TripStop implements Comparable<TripStop> {

	private final String tripId;
	private final String busLine;
	private final String busStopId;
	private final LocalTime scheduledArrivalTime;
	private final TripCalendar calendar;
	private Duration delay;
	private boolean isRealTime = false;

    public TripStop(
			String tripId,
			String busLine,
			String busStopId,
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

	public String getTripId() {
		return tripId;
	}

	public String getBusLine() {
		return busLine;
	}
	
	public String getBusStopId() {
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
		// TODO: KEEP FUTURE AS ATTRIBUTE
		TripStopThreads.schedule( this::clearDelay );
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
				tripId.equals( other.getTripId() ) &&
				busStopId.equals( other.getBusStopId() );
	}

	@Override
	public int compareTo( TripStop o ) {
		return getRealArrivalDateTime().compareTo(
				o.getRealArrivalDateTime() );
	}

	@Override
	public String toString() {
        return String.format( "TripStop=[tripId=%s, busLine=%s, busStopId=%s, " +
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