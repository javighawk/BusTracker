package com.bustracker.bus;

import java.time.LocalTime;

import rx.Observable;

public interface TripStop {
	
	LocalTime getRealArrivalTime();
	
	LocalTime getScheduledArrivalTime();
	
	boolean isRunningToday();
	
	Observable<TripStop> getTrackingTimeoutEvents();
}
