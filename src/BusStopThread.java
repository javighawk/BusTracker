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

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

public class BusStopThread extends Thread {

	/* Attributes */
	private String busStopName;
	private Set<String> stopIDs;
	private Map<String, Bus> allBuses;
	private SortedSet<Bus> upcomingBuses;
	private final long threadPeriod_ms = 10000;
	private long threadLastUpdate_ms = 0;

	
	/*
	 * Constructor
	 * @param busStopName Name of the bus stop to be tracked
	 */
	public BusStopThread(String busStopName){
		this.busStopName = busStopName;
		this.stopIDs = Main.gtfsdata.getBusStopIDs(busStopName);
		this.allBuses = getAllTripToBus(busStopName);
		
		// Update upcoming buses information
		updateBusStopThread(new HashMap<String, Integer>());
	}


	private Map<String, Bus> getAllTripToBus(String busStopName) {
		Map<String, Bus> tripToBus = new HashMap<String, Bus>();
		for(Map<String, String> wt : Main.gtfsdata.getBusStopTimes(busStopName)){ 
			try {
				Map<String, String> calendar = Main.gtfsdata.getCaledarFromTripID(wt.get("trip_id"));
				tripToBus.put(wt.get("trip_id"), 
								new Bus(wt.get("trip_id"), 
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
	public synchronized Bus getUpcomingBus(int idx, boolean goesToCityCentre){
		int k = 0;
		for (Bus b : this.upcomingBuses){
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

	
	private SortedSet<Bus> getNextBuses(Map<String, Integer> tripToDelay, int numberOfBuses) throws Exception{		
		Map<String, Bus> map = getUpdatedAllTripToBuses(tripToDelay);
		SortedSet<Bus> upcomingBbusesToCityCentre = getUpcomingBuses(map, true);
		SortedSet<Bus> upcomingBusesFromCityCentre = getUpcomingBuses(map, false);
		SortedSet<Bus> ret = new TreeSet<Bus>(Main.waitTimeComp);
		ret.addAll(getNUpcomingBuses(upcomingBbusesToCityCentre, numberOfBuses));
		ret.addAll(getNUpcomingBuses(upcomingBusesFromCityCentre, numberOfBuses));
		return ret;
	}
	
	
	private SortedSet<Bus> getNUpcomingBuses(SortedSet<Bus> upcomingBuses, int numberOfBuses) {
		SortedSet<Bus> ret = new TreeSet<Bus>(Main.waitTimeComp);
		Iterator<Bus> iter = upcomingBuses.iterator();
		for (int i=0 ; i<numberOfBuses ; i++){
			if (iter.hasNext()) {
				ret.add(iter.next());
			} else {
				break;
			}
		}
		return ret;
	}
	
	
	private SortedSet<Bus> getUpcomingBuses(Map<String, Bus> tripToBus, boolean goesToCityCentre) {
		// Force day change at 3AM instead of midnight
		LocalDate today = LocalDateTime.now(ZoneId.of(Main.gtfsdata.getTimeZone())).minusHours(3).toLocalDate();
		LocalTime ref = LocalTime.now(ZoneId.of(Main.gtfsdata.getTimeZone()));
		SortedSet<Bus> upcomingBuses = new TreeSet<Bus>(Main.waitTimeComp);
		for (String trip : tripToBus.keySet()) {
			Bus wt = tripToBus.get(trip);
			if (wt.getRealTime().compareTo(ref) >= 0 && wt.isRunning(today)){
				if (wt.getCityCentre() == goesToCityCentre)
					upcomingBuses.add(tripToBus.get(trip));
			}
		}
		return upcomingBuses;
	}


	private Map<String, Bus> getUpdatedAllTripToBuses( Map<String, Integer> tripToDelay) {
		Map<String, Bus> map = new HashMap<String, Bus>(this.allBuses);
		for (String trip : tripToDelay.keySet()){
			if (map.containsKey(trip)){
				Bus wt = map.get(trip);
				wt.setDelay(tripToDelay.get(trip));
				wt.resetRealtime();
				map.replace(trip,wt);
			}
		}
		return map;
	}
}