package com.bustracker.bus;

import rx.Observable;

public interface TrackingTimeoutTimer extends Runnable {
	
	void resetTimer();
	
	Observable<Boolean> getTimeoutEvents();
	
	boolean isTimedOut();

}
