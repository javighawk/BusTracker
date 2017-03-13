package com.bustracker.bus;

import java.time.LocalDateTime;

import rx.Observable;

public class TripStopImpl implements TripStop {

	private final String tripId;
	private final String busLine;
	private final LocalDateTime scheduledArrival;
	private long delay;
	private final TrackingTimeoutTimerImpl timeoutTimer = 
			new TrackingTimeoutTimerImpl( 60000 );
	
	public TripStopImpl( 
			String tripId,
			String busLine,
			LocalDateTime scheduledArrival, 
			long delay ) {
		this.tripId = tripId;
		this.busLine = busLine;
		this.scheduledArrival = scheduledArrival;
		this.delay = delay;
	}
	
	@Override
	public LocalDateTime getRealArrivalTime() {
		return scheduledArrival.plusSeconds( delay );
	}

	@Override
	public LocalDateTime getScheduledArrivalTime() {
		return scheduledArrival;
	}

	@Override
	public Observable<TripStop> getTrackingTimeoutEvents() {
		return timeoutTimer.getTimeoutEvents().map( b -> this );
	}

	@Override
	public String getTripId() {
		return tripId;
	}

	@Override
	public String getBusLine() {
		return busLine;
	}

	@Override
	public void setDelay( long delay ) {
		this.delay = delay;
		setUpTimeoutTimer();
	}

	private void setUpTimeoutTimer() {
		
	}

	@Override
	public boolean isTrackingTimedOut() {
		return timeoutTimer.isTimedOut();
	}

	@Override
	public boolean equals( Object obj ) {
	TripStop other = (TripStop) obj;
		return 
			tripId.equals( other.getTripId() ) &&
			busLine.equals( other.getBusLine() ) && 
			scheduledArrival.equals( other.getScheduledArrivalTime() ) && 
			getRealArrivalTime().equals( other.getRealArrivalTime() );
	}
	
	
}