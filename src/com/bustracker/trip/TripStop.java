package com.bustracker.trip;

import com.bustracker.trip.thread.TripStopThreads;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class TripStop implements Comparable<TripStop> {

	private final String tripId;
	private final String busLine;
	private final String busStopId;
	private final LocalTime scheduledArrival;
	private final TripCalendar calendar;
	private Duration delay;
	private boolean isRealTime = false;

	public TripStop(
			String tripId,
			String busLine,
			String busStopId,
			LocalTime scheduledArrival,
			TripCalendar calendar,
			Duration delay ) {
		this.tripId = tripId;
		this.busLine = busLine;
		this.busStopId = busStopId;
		this.scheduledArrival = scheduledArrival;
		this.calendar = calendar;
		this.delay = delay;
	}

    public TripStop(
			String tripId,
			String busLine,
			String busStopId,
			String scheduledArrivalTime,
			TripCalendar calendar,
			Duration delay ) {
		this( 
				tripId, 
				busLine,
				busStopId,
				parseLocalTimeFromString( scheduledArrivalTime ),
				calendar,
				delay );
	}

	private static LocalTime parseLocalTimeFromString( String time ) {
		try {
			return LocalTime.parse( time );
		} catch( DateTimeParseException e ) {
			int hour = Integer.parseInt( time.substring( 0,2 ) ) - 24;
			return LocalTime.parse(
					String.format( "%02d", hour ) + time.substring( 2 ) );
		}
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
		return scheduledArrival;
	}
	
	private LocalTime getRealArrivalTime() {
		return scheduledArrival.plus( delay );
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
				scheduledArrival,
				getRealArrivalDateTime(),
				calendar,
				delay.getSeconds(),
                isRealTime );
	}

	public static TripStopBuilder builder() {
	    return new TripStopBuilder();
    }
}