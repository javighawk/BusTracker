package com.bustracker.bus;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class TripStop implements Comparable<TripStop> {

	private final String tripId;
	private final String busLine;
	private final String busStopId;
	private final LocalTime scheduledArrival;
	private Duration delay;
	private boolean isRealTime = false;
	
	public TripStop( 
			String tripId,
			String busLine,
			String busStopId,
			LocalTime scheduledArrival,
			Duration delay ) {
		this.tripId = tripId;
		this.busLine = busLine;
		this.busStopId = busStopId;
		this.scheduledArrival = scheduledArrival;
		this.delay = delay;
	}
	
	public TripStop( 
			String tripId,
			String busLine,
			String busStopId,
			String scheduledArrivalTime,
			Duration delay ) {
		this( 
				tripId, 
				busLine,
				busStopId,
				parseLocalTimeFromString( scheduledArrivalTime ),
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

	public void setDelay( Duration delay ) {
		this.delay = delay;
	}

	public Duration getDelay( ) {
		return delay;
	}

	public void setIsRealTime( boolean realTime ) {
		this.isRealTime = realTime;
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
		LocalTime realArrivalTime = getRealArrivalTime( );
		return String.format( "TripStop=[tripId=%s, busLine=%s, busStopId=%s, " +
				"schedArrivalTime=%s, " +
				"realArrivalTime=%s, " +
				"delay=%d, realTime=%b]",
				tripId, busLine, busStopId,
				LocalTime.from( scheduledArrival ),
				LocalTime.from( realArrivalTime ),
				delay.getSeconds(), isRealTime );
	}
}