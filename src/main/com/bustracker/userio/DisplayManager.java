package com.bustracker.userio;

public interface DisplayManager {
	
	void showBusLineAndWaitingTime( int busLine, long waitingTime );
	
	void showRealTimeIndicator();
	
	void showUpcomingBusIndex();
	
}
