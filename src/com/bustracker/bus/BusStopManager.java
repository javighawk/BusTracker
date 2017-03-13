package com.bustracker.bus;

import rx.Observable;

public interface BusStopManager {

	Observable<TripStop> getTripStopUpdatedEvents();
}
