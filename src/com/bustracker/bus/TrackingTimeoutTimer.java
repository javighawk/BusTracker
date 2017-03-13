package com.bustracker.bus;

import rx.Observable;

public interface TrackingTimeoutTimer {
	
	void resetTimer();
	
	Observable<Boolean> getTimeoutEvents();
	
	boolean isTimedOut();

}
