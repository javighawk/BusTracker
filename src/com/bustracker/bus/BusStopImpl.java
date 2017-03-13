package com.bustracker.bus;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.lang.Exception;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import com.bustracker.Main;
import com.bustracker.userio.Display;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;


public class BusStopImpl extends Thread {

	/* Attributes */
	private String busStopName;
	private Set<String> stopIDs;
	private Map<String, TripStopImpl> allBuses;
	private SortedSet<TripStopImpl> upcomingBuses;
	private final long threadPeriod_ms = 10000;
	private long threadLastUpdate_ms = 0;

	
	/*
	 * Constructor
	 * @param busStopName Name of the bus stop to be tracked
	 */
	public BusStopImpl(String busStopName){
		this.busStopName = busStopName;
		this.stopIDs = Main.gtfsdata.getBusStopIDs(busStopName);
		this.allBuses = getAllTripToBus(busStopName);
		
		// Update upcoming buses information
		updateBusStopThread(new HashMap<String, Integer>());
	}


	private Map<String, TripStopImpl> getAllTripToBus(String busStopName) {
		Map<String, TripStopImpl> tripToBus = new HashMap<String, TripStopImpl>();
		for(Map<String, String> wt : Main.gtfsdata.getBusStopTimes(busStopName)){ 
			try {
				Map<String, String> calendar = Main.gtfsdata.getCaledarFromTripID(wt.get("trip_id"));
				tripToBus.put(wt.get("trip_id"), 
								new TripStopImpl(wt.get("trip_id"), 
											 this.busStopName, 
											 wt.get("arrival_time"),
											 calendar));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tripToBus;
	}

	
	public void run(){
		Map<String, Integer> updates;
		while(true){
			try{
				FeedMessage feed = FeedMessage.parseFrom(Main.url.openStream());
				updates = getTripToDelayFromFeed(feed);
			} 
			catch (IOException e){
				updates = new HashMap<String, Integer>();
			}
			updateBusStopThread(updates);
			while (System.currentTimeMillis() - threadLastUpdate_ms < threadPeriod_ms);
		}
	}


	private void updateBusStopThread(Map<String, Integer> updates) {
		try{				
			upcomingBuses = getNextBuses(updates, Display.numOfBusesToShow);
			threadLastUpdate_ms = System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param idx 0 for the upcoming bus, 1 for the following bus, etc.
	 */
	public synchronized TripStopImpl getUpcomingBus(int idx, boolean goesToCityCentre){
		int k = 0;
		for (TripStopImpl b : this.upcomingBuses){
			if (b.getCityCentre() == goesToCityCentre){
				if (k == idx)
					return b;
				else
					k++;
			}
		}
		return null;
	}
	
	
	private Map<String, Integer> getTripToDelayFromFeed(FeedMessage feed) {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		for (FeedEntity entity : feed.getEntityList()) {
			if (entity.hasTripUpdate()) {								  
				ret.putAll(getTripToDelayFromTripUpdate(entity.getTripUpdate()));
			}
		}
		return ret;
	}
	
	
	private Map<String, Integer> getTripToDelayFromTripUpdate(TripUpdate tripUpdate) {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		try {
			String tripID = tripUpdate.getTrip().getTripId();
			String routeNumber = Main.gtfsdata.getBusNumberFromRoute(tripUpdate.getTrip().getRouteId());
			String tripDirection = Main.gtfsdata.getTripDirection(tripID);
			if (routeNumber != null && tripDirection != null) {
				for (StopTimeUpdate stu : tripUpdate.getStopTimeUpdateList()){
					if (this.stopIDs.contains(stu.getStopId())){
						ret.put(tripID, stu.getArrival().getDelay());
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	
	private SortedSet<TripStopImpl> getNextBuses(Map<String, Integer> tripToDelay, int numberOfBuses) throws Exception{		
		Map<String, TripStopImpl> map = getUpdatedAllTripToBuses(tripToDelay);
		SortedSet<TripStopImpl> upcomingBbusesToCityCentre = getUpcomingBuses(map, true);
		SortedSet<TripStopImpl> upcomingBusesFromCityCentre = getUpcomingBuses(map, false);
		SortedSet<TripStopImpl> ret = new TreeSet<TripStopImpl>(Main.waitTimeComp);
		ret.addAll(getNUpcomingBuses(upcomingBbusesToCityCentre, numberOfBuses));
		ret.addAll(getNUpcomingBuses(upcomingBusesFromCityCentre, numberOfBuses));
		return ret;
	}
	
	
	private SortedSet<TripStopImpl> getNUpcomingBuses(SortedSet<TripStopImpl> upcomingBuses, int numberOfBuses) {
		SortedSet<TripStopImpl> ret = new TreeSet<TripStopImpl>(Main.waitTimeComp);
		Iterator<TripStopImpl> iter = upcomingBuses.iterator();
		for (int i=0 ; i<numberOfBuses ; i++){
			if (iter.hasNext()) {
				ret.add(iter.next());
			} else {
				break;
			}
		}
		return ret;
	}
	
	
	private SortedSet<TripStopImpl> getUpcomingBuses(Map<String, TripStopImpl> tripToBus, boolean goesToCityCentre) {
		// Force day change at 3AM instead of midnight
		LocalDate today = LocalDateTime.now(ZoneId.of(Main.gtfsdata.getTimeZone())).minusHours(3).toLocalDate();
		LocalTime ref = LocalTime.now(ZoneId.of(Main.gtfsdata.getTimeZone()));
		SortedSet<TripStopImpl> upcomingBuses = new TreeSet<TripStopImpl>(Main.waitTimeComp);
		for (String trip : tripToBus.keySet()) {
			TripStopImpl wt = tripToBus.get(trip);
			if (wt.getRealTime().compareTo(ref) >= 0 && wt.isRunning(today)){
				if (wt.getCityCentre() == goesToCityCentre)
					upcomingBuses.add(tripToBus.get(trip));
			}
		}
		return upcomingBuses;
	}


	private Map<String, TripStopImpl> getUpdatedAllTripToBuses( Map<String, Integer> tripToDelay) {
		Map<String, TripStopImpl> map = new HashMap<String, TripStopImpl>(this.allBuses);
		for (String trip : tripToDelay.keySet()){
			if (map.containsKey(trip)){
				TripStopImpl wt = map.get(trip);
				wt.setDelay(tripToDelay.get(trip));
				wt.resetRealtime();
				map.replace(trip,wt);
			}
		}
		return map;
	}
}