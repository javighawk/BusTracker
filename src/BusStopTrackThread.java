import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
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

public class BusStopTrackThread extends Thread {

	/* Attributes */
	private String busStopName;
	private Set<String> stop_ids;
	private Map<String, WaitTime> stop_times;
	private SortedSet<WaitTime> upcomingBuses;
	private long BUSTRACK_PERIOD_MS;

	
	/*
	 * Constructor
	 * @param busStopName Name of the bus stop to be tracked
	 */
	public BusStopTrackThread(String busStopName){
		// Set default argument values
		this.BUSTRACK_PERIOD_MS = 10000;
		
		// Save bus stop name
		this.busStopName = busStopName;
		
		// Retrieve the bus stop IDs
		this.stop_ids = Main.gtfsdata.getBusStopIDs(busStopName);
		
		// Initialize sorted set storing the stop times
		this.stop_times = new HashMap<String, WaitTime>();
		
		// Retrieve the stop times for all trips
		for(Map<String, String> wt : Main.gtfsdata.getBusStopTimes(busStopName)){ 
			// Add a new WaitTime object
			try {
				// Get calendar info
				Map<String, String> cal = Main.gtfsdata.getCaledarFromTripID(wt.get("trip_id"));
				
				
				stop_times.put(wt.get("trip_id"), 
								new WaitTime(wt.get("trip_id"), 
											this.busStopName, 
											wt.get("arrival_time"),
											cal));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Update upcoming buses information
		try {
			this.upcomingBuses = getNextWaitTimes(new HashMap<String, Integer>(), Display.numOfBusesToShow);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/*
	 * Main thread
	 */
	public void run(){
		// Keep track of the period of the thread
		long period;
		
		// Start thread loop
		while(true){
			// Update period
			period = System.currentTimeMillis();
			
			// Declare updates
			Map<String, Integer> updates = new HashMap<String, Integer>();
			
			try{
				// Get FeedMessage
				FeedMessage feed = FeedMessage.parseFrom(Main.url.openStream());
	
				// Extract trip updates
				updates = getTripUpdates(feed);
			} 
			catch (MalformedURLException e) {e.printStackTrace();} 
			catch (IOException e) {} 
			catch (Exception e){e.printStackTrace();}
			
			try{				
				// Update upcoming buses information
				this.upcomingBuses = getNextWaitTimes(updates, Display.numOfBusesToShow);
				
			} catch (Exception e) {e.printStackTrace();}
			
			// Wait until BUSTRACK_PERIOD_MS has passed
			while (System.currentTimeMillis() - period < BUSTRACK_PERIOD_MS);
		}
	}
	
	
	/*
	 * Getter
	 * @return Bus stop name
	 */
	public String getBusStopName(){
		return this.busStopName;
	}

	
	/*
	 * Get a WaitTime object representing an upcoming bus
	 * @param idx 0 for the upcoming bus, 1 for the following bus, etc.
	 * @param cityCentre True if the bus is going to City Centre, false otherwise
	 * @return WaitTime representing this bus
	 */
	public WaitTime getUpcomingBus(int idx, boolean cityCentre){
		// Keep track of the index of the buses coming on the desired direction
		int k = 0;
		
		// Iterate through all the upcoming buses
		for (WaitTime wt : this.upcomingBuses){
			if (wt.getCityCentre() == cityCentre){
				if (k == idx)
					return wt;
				else
					k++;
			}
		}
		
		return null;
	}
	
	/*
	 * Extract trip updates from a FeedMessage object only for this bus stop
	 * @param feed FeedMessage object extracted from GTFS realtime
	 * @return Set of (Trip id, delay) mappings representing the Trip updates
	 * @throws Exception From GTFSData class
	 * @throws ParseException Time parsing error
	 */
	private Map<String, Integer> getTripUpdates(FeedMessage feed) throws ParseException, Exception{
		// Initialize returning set
		Map<String, Integer> ret = new HashMap<String, Integer>();
		
		// Iterate through all the FeedEntities
		for (FeedEntity entity : feed.getEntityList()) {
			// Check if it has a Trip update
			if (entity.hasTripUpdate()) {								  
				// Get trip update
				TripUpdate tu = entity.getTripUpdate();
				
				// Get trip ID
				String tripID = tu.getTrip().getTripId();
				
				// Get route number
				String routeNumber = Main.gtfsdata.getBusNumberFromRoute(tu.getTrip().getRouteId());
				
				// Get trip direction
				String tripDirection = Main.gtfsdata.getTripDirection(tripID);
				
				// Check if route or trip exist in the GTFS files
				if (routeNumber == null || tripDirection == null) {
					continue;
				}
				
				// Iterate through all the Stop updates
				for (StopTimeUpdate stu : tu.getStopTimeUpdateList()){
					// Work only with the stops of interest
					if (this.stop_ids.contains(stu.getStopId())){
						// Add entry to the map
						ret.put(tripID, stu.getArrival().getDelay());
					}
				}
			}
		}
		
		return ret;
	}

	
	/*
	 * Retrieve the waiting times of the next upcoming X trips
	 * @param delay Map with the trip IDs as the keys and the delay in seconds as the value
	 * @param nTimes Number of stop times to retrieve
	 * @return Sorted set of WaitTime objects
	 * @throws Exception from GTFS query
	 */
	private SortedSet<WaitTime> getNextWaitTimes(Map<String, Integer> delay, int nTimes) throws Exception{		
		// Get today's date (force day change at 3AM instead of midnight)
		LocalDate today = LocalDateTime.now(ZoneId.of(Main.gtfsdata.getTimeZone())).minusHours(3).toLocalDate();
		
		// Make a copy of the stop_trip Map
		Map<String, WaitTime> map = new HashMap<String, WaitTime>(this.stop_times);
		
		// Add delay to the given trips
		for (String trip : delay.keySet()){
			if (map.containsKey(trip)){
				// Create new WaitTime object and add delay
				WaitTime wt = map.get(trip);
				wt.setDelay(delay.get(trip));
				
				// Replace it on the Map object
				map.replace(trip,wt);
			}
		}

		// Make a set of the initial map with the future trips going on both directions running today
		SortedSet<WaitTime> wt_cityCentre_true = new TreeSet<WaitTime>(Main.waitTimeComp);
		SortedSet<WaitTime> wt_cityCentre_false = new TreeSet<WaitTime>(Main.waitTimeComp);
		
		// Create a reference WaitTime object
		LocalTime ref = LocalTime.now(ZoneId.of(Main.gtfsdata.getTimeZone()));
		
		// Iterate through all the map entries
		for (String trip : map.keySet()) {
			// Extract WaitTime object
			WaitTime wt = map.get(trip);
			
			if (wt.getRealTime().compareTo(ref) >= 0 && wt.isRunning(today)){
				// Check direction
				if (wt.getCityCentre())
					wt_cityCentre_true.add(map.get(trip));
				else
					wt_cityCentre_false.add(map.get(trip));
			}
		}
		
		// Return the first nTimes objects in the sorted set for each of the directions
		SortedSet<WaitTime> ret = new TreeSet<WaitTime>(Main.waitTimeComp);
		Iterator<WaitTime> iter_cityCentre_true = wt_cityCentre_true.iterator();
		Iterator<WaitTime> iter_cityCentre_false = wt_cityCentre_false.iterator();
		for (int i=0 ; i<nTimes ; i++){
			ret.add(iter_cityCentre_true.next());
			ret.add(iter_cityCentre_false.next());
		}
		
		return ret;
	}
}