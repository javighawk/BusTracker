package com.bustracker.bus;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class TripStop implements Comparable<TripStop> {

	private final String tripId;
	private final Optional<String> busLine;
	private final String busStopId;
	private final Optional<LocalTime> scheduledArrival;
	private Duration delay;
	private boolean isRealTime = false;

	public TripStop(
			String tripId,
			String busLine,
			String busStopId,
			LocalTime scheduledArrival,
			Duration delay ) {
		this.tripId = tripId;
		this.busLine = Optional.ofNullable( busLine );
		this.busStopId = busStopId;
		this.scheduledArrival = Optional.ofNullable( scheduledArrival );
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

	public Optional<String> getBusLine() {
		return busLine;
	}
	
	public String getBusStopId() {
		return busStopId;
	}

	public Optional<LocalTime> getScheduledArrivalTime() {
		return scheduledArrival;
	}
	
	public Optional<LocalTime> getRealArrivalTime() {
		return scheduledArrival.map(
		        t -> t.plus( delay ) );
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
	    if( !getRealArrivalTime().isPresent() ||
            !o.getRealArrivalTime().isPresent() ) {
	        throw new UnsupportedOperationException( "Arrival time not set. Cannot compare" );
        } else {
            return getRealArrivalTime().get().compareTo(
                    o.getRealArrivalTime().get() );
        }
	}

	@Override
	public String toString() {
        return String.format( "TripStop=[tripId=%s, busLine=%s, busStopId=%s, " +
				"schedArrivalTime=%s, " +
				"realArrivalTime=%s, " +
				"delay=%d, realTime=%b]",
				tripId, busLine, busStopId,
				scheduledArrival.orElse( null ),
                getRealArrivalTime().orElse( null ),
				delay.getSeconds(),
                isRealTime );
	}

	public static TripStopBuilder builder() {
	    return new TripStopBuilder();
    }
}