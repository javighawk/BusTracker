import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.Exception;
import java.time.LocalTime;
import java.time.ZoneId;
import static java.time.temporal.ChronoUnit.MINUTES;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

public class BusTracker {
	
	/* Initialize user variables */
	public static String gtfsPath = "gtfs/";
	public static String gtfsURL = "http://apps2.saskatoon.ca/app/data/TripUpdate/TripUpdates.pb";
	public static String busStopName = "22nd Street / Avenue M";
	
	/* Declare global variables */
	public static GTFSData gtfsdata = new GTFSData();
	public static Set<String> stop_ids = new HashSet<String>();
	public static URL url;
	public static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	/*
	 * Initialize bus tracker 
	 */
	public static void initialize(){
		// Trace
		System.out.println("start");
		
		// Load GTFS data
		gtfsdata.parseFromPath(gtfsPath);
		
		// Trace
		System.out.println("Parsed");
		
		// Get the stop IDs of the desired bus stops
		Map<String, String> busStopMap = new HashMap<String, String>();
		busStopMap.put("stop_name", busStopName);
		Set<Map<String, String>> stops = gtfsdata.getMapFromData(gtfsdata.stops, busStopMap);
		
		// Store stops IDs in a global set
		for (Map<String, String> m : stops)
			stop_ids.add(m.get("stop_id"));
		
		// Trace
		System.out.println("End init");
	}
	
	
	/*
	 * Main function
	 */
	public static void main(String[] args){
		// Initialize system
		initialize();

		// Start Trip update retrieval
		try {
			// Trace
			System.out.println("Initialize");
			
			// Initialize URL
			url = new URL(gtfsURL);
			
			// Get FeedMessage
			FeedMessage feed = FeedMessage.parseFrom(url.openStream());

			// Iterate through all the FeedEntities
			for (FeedEntity entity : feed.getEntityList()) {
				// Check if it has a Trip update
				if (!entity.hasTripUpdate()) {
					continue;
				}
							  
				// Get trip update
				TripUpdate tu = entity.getTripUpdate();
				
				// Get route number
				String routeNumber = gtfsdata.getRouteNumber(tu.getTrip().getRouteId());
				
				// Get trip direction
				String tripDirection = gtfsdata.getTripDirection(tu.getTrip().getTripId());
				
				if (!tripDirection.equals("City Centre"))
					continue;
				
				// Iterate through all the Stop updates
				for (StopTimeUpdate stu : tu.getStopTimeUpdateList()){
					
					// Work only with the stops of interest
					if (stop_ids.contains(stu.getStopId())) {
						long wait = getWaitingTime(gtfsdata.getScheduledArrivalTime(tu.getTrip().getTripId(), stu.getStopId()),
													stu.getArrival().getDelay(),
													gtfsdata.getTimeZone());

						System.out.print(routeNumber + " " + tripDirection + ": ");
						System.out.println(wait + " min");
					}
				}
				  
				 }			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("End");
	}

	
	/*
	 * Calculate the waiting time for a bus given the scheduled arrival time and the delay
	 * @param schedTime Scheduled time as String (extracted from GTFS static data)
	 * @param delaySec Delay in seconds (as extracted from the Trip update)
	 * @param timezone Timezone of the bus agency (extracted from GTFS static data)
	 * @return Delay in minutes
	 * @throws ParseException When scheduled arrival format is not valid
	 */
	public static long getWaitingTime(String schedTime, int delaySec, String timezone) throws ParseException{
		// Get current time in the Bus agency local time
		LocalTime now = LocalTime.now(ZoneId.of(timezone));
		
		// Parse scheduled arrival (this is in the bus agency's time zone)
		LocalTime sched = LocalTime.parse(schedTime);
		
		// Get the difference in time
		long diff = now.until(sched, MINUTES);
		
		// Add the delay
		diff += (long) (delaySec / 60);
		
		// Return waiting time in minutes
		return diff;
	}
}