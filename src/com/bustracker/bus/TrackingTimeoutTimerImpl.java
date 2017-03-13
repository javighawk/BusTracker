package com.bustracker.bus;

import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.subjects.PublishSubject;

public class TrackingTimeoutTimerImpl implements TrackingTimeoutTimer {
	
	private final long timeout;
	private long lastResetTime;
	private final AtomicReference<Boolean> isTimedOut = 
			new AtomicReference<>( Boolean.FALSE );
	private final PublishSubject<Boolean> timeoutSubject = 
			PublishSubject.create(); 

	public TrackingTimeoutTimerImpl( long timeoutValue ) {
		this.timeout = timeoutValue;
	}
	
	@Override
	public void run() {
		while( true ) {
			if( System.currentTimeMillis() - lastResetTime < timeout ) {
				clearTimeOut();
			} else {
				setTimeOut();
			}
			try {
				Thread.sleep( 100 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void resetTimer() {
		lastResetTime = System.currentTimeMillis();
	}
	
	private void setTimeOut() {
		isTimedOut.updateAndGet( s -> {
			if( s ) {
				fireTimedOutEvent( Boolean.FALSE );
			}
			return Boolean.FALSE;
		} );
	}
	
	private void clearTimeOut() {
		isTimedOut.updateAndGet( s -> {
			if( !s ) {
				fireTimedOutEvent( Boolean.TRUE );
			}
			return Boolean.FALSE;
		} );
	}

	private void fireTimedOutEvent( Boolean timedout ) {
		timeoutSubject.onNext( timedout );
	}
	
	@Override
	public Observable<Boolean> getTimeoutEvents() {
		return timeoutSubject.asObservable();
	}

	@Override
	public boolean isTimedOut() {
		return isTimedOut.get();
	}

}
