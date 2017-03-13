package com.bustracker.bus;

import java.time.LocalDateTime;
import java.time.LocalTime;

import rx.Observable;

public interface TripStop {
	
	String getTripId();
	
	String getBusLine();
	
	LocalDateTime getRealArrivalTime();
	
	LocalDateTime getScheduledArrivalTime();
	
	Observable<TripStop> getTrackingTimeoutEvents();

	void setDelay( long delay );
	
	boolean isTrackingTimedOut();
}
