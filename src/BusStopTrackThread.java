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

public class BusStopTrackThread extends Thread {
	
	/* Bus stop name */
	private String busStopName;
	
	/* Attributes */
	private Set<String> stop_ids = new HashSet<String>();
	public URL url;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private long BUSTRACK_PERIOD_MS = 10000;

	
	/*
	 * Constructor
	 * @param busStopName Name of the bus stop to be tracked
	 * @param url URL pointing to the Trip Update data
	 */
	public BusStopTrackThread(String busStopName, String url){
		// Save bus stop name
		this.busStopName = busStopName;
		
		// Retrieve the bus stop IDs
		this.stop_ids = Main.gtfsdata.getBusStopIDs(busStopName);
		
		// Intialize URL
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	
	/*
	 * Main thread
	 */
	public void run(){
		try {
			while(true){
				// Trace
				System.out.println("Initialize");
				
				// Get FeedMessage
				FeedMessage feed = FeedMessage.parseFrom(this.url.openStream());
	
				// Iterate through all the FeedEntities
				for (FeedEntity entity : feed.getEntityList()) {
					// Check if it has a Trip update
					if (entity.hasTripUpdate()) {								  
						// Get trip update
						TripUpdate tu = entity.getTripUpdate();
						
						// Get route number
						String routeNumber = Main.gtfsdata.getRouteNumber(tu.getTrip().getRouteId());
						
						// Get trip direction
						String tripDirection = Main.gtfsdata.getTripDirection(tu.getTrip().getTripId());
						
						if (!tripDirection.equals("City Centre"))
							continue;
						
						// Iterate through all the Stop updates
						for (StopTimeUpdate stu : tu.getStopTimeUpdateList()){
							
							// Work only with the stops of interest
							if (stop_ids.contains(stu.getStopId())) {
								long wait = getWaitingTime(Main.gtfsdata.getScheduledArrivalTime(tu.getTrip().getTripId(), stu.getStopId()),
															stu.getArrival().getDelay(),
															Main.gtfsdata.getTimeZone());
		
								System.out.print(routeNumber + " " + tripDirection + ": ");
								System.out.println(wait + " min");
							}
						}
					}
				}
				// Delay
				Thread.sleep(BUSTRACK_PERIOD_MS);
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
	private long getWaitingTime(String schedTime, int delaySec, String timezone) throws ParseException{
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