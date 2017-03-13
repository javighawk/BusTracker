package com.bustracker.bus;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Iterables;

import rx.Observable;
import rx.subjects.PublishSubject;

public class BusStopImpl implements BusStop {

	private final int numberOfBuses;
	private SortedSet<TripStop> trips = new TreeSet<>();
	private final PublishSubject<BusStop> tripsUpdateSubject = 
			PublishSubject.create();
	
	public BusStopImpl( int numberOfBuses ) {
		this.numberOfBuses = numberOfBuses;
	}
	
	@Override
	public void setNewUpdates(Set<TripStop> tripStops) {
		boolean fireEvent = false;
		for( TripStop ts : tripStops ) {
			if( !trips.contains( ts ) && trips.last().compareTo( ts ) < 0 ) {
				trips.add( ts );
				trips.remove( trips.last() );
				fireEvent = true;
			} else {
				trips.forEach( t -> {
					if( t.equals( ts ) ) {
						t.setDelay( ts.getDelay() );
					}
				} );
			}
		}
	}

	@Override
	public TripStop getUpcomingBus( int busIndex ) {
		return Iterables.get( trips, busIndex );
	}
	
	private void fireTripUpdateEvent() {
		tripsUpdateSubject.onNext( this );
	}

	@Override
	public Observable<BusStop> getNewUpdatesEvents() {
		return tripsUpdateSubject.asObservable();
	}

}
