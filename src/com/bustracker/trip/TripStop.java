package com.bustracker.trip;

import com.bustracker.trip.thread.TripStopThreads;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

public class TripStop implements Comparable<TripStop> {

	private final String tripId;
	private final String busLine;
	private final String busStopId;
	private final LocalTime scheduledArrival;
	private final Set<DayOfWeek> operatingWeekdays;
	private Duration delay;
	private boolean isRealTime = false;

	public TripStop(
			String tripId,
			String busLine,
			String busStopId,
			LocalTime scheduledArrival,
			Set<DayOfWeek> operatingWeekdays,
			Duration delay ) {
		this.tripId = tripId;
		this.busLine = busLine;
		this.busStopId = busStopId;
		this.scheduledArrival = scheduledArrival;
		this.operatingWeekdays = operatingWeekdays;
		this.delay = delay;
	}
	
	public TripStop(
			String tripId,
			String busLine,
			String busStopId,
			String scheduledArrivalTime,
			Set<DayOfWeek> operatingWeekdays,
			Duration delay ) {
		this( 
				tripId, 
				busLine,
				busStopId,
				parseLocalTimeFromString( scheduledArrivalTime ),
				operatingWeekdays,
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
	
	public LocalTime getRealArrivalTime() {
		return scheduledArrival.plus( delay );
	}

	public Set<DayOfWeek> getOperatingWeekdays() {
		return operatingWeekdays;
	}

	public void setDelay( Duration delay ) {
		this.delay = delay;
		this.isRealTime = true;
		// KEEP FUTURE AS ATTRIBUTE
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
		return getRealArrivalTime().compareTo(
				o.getRealArrivalTime() );
	}

	@Override
	public String toString() {
        return String.format( "TripStop=[tripId=%s, busLine=%s, busStopId=%s, " +
				"schedArrivalTime=%s, " +
				"realArrivalTime=%s, " +
				"operatingWeekdays=%s, " +
				"delay=%d, realTime=%b]",
				tripId, busLine, busStopId,
				scheduledArrival,
                getRealArrivalTime(),
				operatingWeekdays,
				delay.getSeconds(),
                isRealTime );
	}

	public static TripStopBuilder builder() {
	    return new TripStopBuilder();
    }
}