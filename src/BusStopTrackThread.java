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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import static java.time.temporal.ChronoUnit.MINUTES;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

public class BusStopTrackThread extends Thread {

	/* Attributes */
	private String busStopName;
	private boolean cityCentre;
	private Set<String> stop_ids;
	private Map<String, WaitTime> stop_times;
	private long BUSTRACK_PERIOD_MS;

	
	/*
	 * Constructor
	 * @param busStopName Name of the bus stop to be tracked
	 * @param url URL pointing to the Trip Update data
	 */
	public BusStopTrackThread(String busStopName, String url){
		// Set default argument values
		this.cityCentre = false;
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
	}

	
	/*
	 * Main thread
	 */
	public void run(){
		while(true){
			try{
				// Get FeedMessage
				FeedMessage feed = FeedMessage.parseFrom(Main.url.openStream());
	
				// Extract trip updates
				Map<String, Integer> updates = getTripUpdates(feed);
				
				// Get the next 3 waiting times
				Set<WaitTime> times = getNextWaitTimes(updates, 3);
				
				for (WaitTime s : times) {
					System.out.print(Main.gtfsdata.getBusNumberFromTrip(s.getTripID()) + " " + Main.gtfsdata.getTripDirection(s.getTripID()) + ": ");
					System.out.println(getWaitingTime(s.getSchedTime().toString(), s.getDelay()));
				}
				
				// Delay
				Thread.sleep(BUSTRACK_PERIOD_MS);
			} 
			catch (MalformedURLException e) {e.printStackTrace();} 
			catch (IOException e) {} 
			catch (Exception e) {e.printStackTrace();}
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
	 * Getter
	 * @return Direction to city centre
	 */
	public boolean getCityCentreDirection(){
		return this.cityCentre;
	}
	
	
	/*
	 * Switch direction
	 */
	public void switchCityCentreDirection(){
		this.cityCentre = !this.cityCentre;
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
	 * Get waiting time given a scheduled arrival and a delay
	 * @param schedArrival Scheduled arrival of the bus
	 * @param delay Delay in seconds
	 * @return 
	 */
	private long getWaitingTime(String schedArrival, long delay){
		// Get current time in the Bus agency local time
		LocalTime now = LocalTime.now(ZoneId.of(Main.gtfsdata.getTimeZone()));
		
		// Initialize time difference in minutes
		long diff;
		
		try {
			// Parse scheduled arrival (this is in the bus agency's time zone)
			LocalTime schedTime = LocalTime.parse(schedArrival);					
			
			// Get the difference in time
			diff = now.until(schedTime, MINUTES);
			
		} catch (DateTimeParseException e) {
			// Extract the hour value and subtract "24"
			int hour = Integer.parseInt(schedArrival.substring(0,2)) - 24;
			
			// Parse scheduled arrival (this is in the bus agency's time zone)
			LocalTime schedTime = LocalTime.parse(String.format("%02d", hour) + schedArrival.substring(2));
			
			// Get the difference in time (add the 24 hours that we subtracted before)
			diff = now.until(schedTime, MINUTES) + 24*60;
		}
		
		// Add delay
		diff += (int)(delay/60);
		
		// Subtract 1 minute to wait time to avoid unwanted missed buses!
		if (diff > 0)
			diff -= 1;
		
		return diff;		
	}
	
	
	/*
	 * Retrieve the waiting times of the next upcoming X trips
	 * @param delay Map with the trip IDs as the keys and the delay in seconds as the value
	 * @param nTimes Number of stop times to retrieve
	 * @return Sorted set of WaitTime objects
	 * @throws Exception from GTFS query
	 */
	private SortedSet<WaitTime> getNextWaitTimes(Map<String, Integer> delay, int nTimes) throws Exception{		
		// Get today's date
		LocalDate today = LocalDate.now(ZoneId.of(Main.gtfsdata.getTimeZone()));
		
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

		// Make a set of the initial map with the future trips going to the selected direction running today
		SortedSet<WaitTime> aux = new TreeSet<WaitTime>(Main.waitTimeComp);
		
		// Create a reference WaitTime object
		LocalTime ref = LocalTime.now(ZoneId.of(Main.gtfsdata.getTimeZone()));

		// Iterate through all the map entries
		for (String trip : map.keySet()) {
			// Extract WaitTime object
			WaitTime wt = map.get(trip);
			
			if (wt.getRealTime().compareTo(ref) >= 0 && 
				this.cityCentre == wt.getCityCentre() &&
				wt.isRunning(today))
				aux.add(map.get(trip));
		}
		
		// Return the first nTimes objects in the sorted set
		SortedSet<WaitTime> ret = new TreeSet<WaitTime>(Main.waitTimeComp);
		Iterator<WaitTime> iter = aux.iterator();
		for (int i=0 ; i<nTimes ; i++){
			WaitTime wt = iter.next();
			ret.add(wt);
		}
		
		return ret;
	}
}