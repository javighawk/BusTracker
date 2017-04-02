package com.bustracker.bus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import rx.Observable;
import rx.subjects.PublishSubject;

public class TripStopImpl implements TripStop {

	private final String tripId;
	private final String busLine;
	private final LocalDateTime scheduledArrival;
	private long delay;
	private final TrackingTimeoutTimerImpl timeoutTimer = 
			new TrackingTimeoutTimerImpl( 60000 );
	private PublishSubject<TripStop> delaySubject = PublishSubject.create();
	private final String busStopId;
	
	public TripStopImpl( 
			String tripId,
			String busLine,
			String busStopId,
			LocalDateTime scheduledArrival, 
			long delay ) {
		this.tripId = tripId;
		this.busLine = busLine;
		this.busStopId = busStopId;
		this.scheduledArrival = scheduledArrival;
		this.delay = delay;
		timeoutTimer.start();
	}
	
	public TripStopImpl( 
			String tripId,
			String busLine,
			String busStopId,
			String scheduledArrivalTime, 
			long delay ) {
		this( 
				tripId, 
				busLine,
				busStopId,
				LocalDateTime.of(
						LocalDate.now(),
						LocalTime.parse( 
								scheduledArrivalTime ) ), 
				delay );
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
	public String getBusStopId() {
		return busStopId;
	}

	@Override
	public void setDelay( long delay ) {
		if( delay != this.delay ) {
			this.delay = delay;
			resetTimeoutTimer();
			fireDelayUpdateEvent();
		}
	}

	private void fireDelayUpdateEvent() {
		delaySubject .onNext( this );
	}

	private void resetTimeoutTimer() {
		timeoutTimer.resetTimer();
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
			scheduledArrival.equals( other.getScheduledArrivalTime() );
	}

	@Override
	public int compareTo( TripStop o ) {
		return getRealArrivalTime().compareTo( o.getRealArrivalTime() );
	}

	@Override
	public long getDelay() {
		return delay;
	}

	@Override
	public Observable<TripStop> getDelayUpdateEvents() {
		return delaySubject.asObservable();
	}
	
}