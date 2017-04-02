package com.bustracker.bus;

import java.time.LocalDateTime;

import rx.Observable;

public interface TripStop extends Comparable<TripStop> {
	
	String getTripId();
	
	String getBusLine();
	
	String getBusStopId();
	
	LocalDateTime getScheduledArrivalTime();

	long getDelay();
	
	LocalDateTime getRealArrivalTime();
	
	Observable<TripStop> getDelayUpdateEvents();
	
	Observable<TripStop> getTrackingTimeoutEvents();

	void setDelay( long delay );
	
	boolean isTrackingTimedOut();
	
}
